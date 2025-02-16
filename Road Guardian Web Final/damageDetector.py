# Only works on Python 3.11 & below. Install these if not already. Version number MATTERS! Install YOLOv7 last.
#!pip install firebase-admin
#!pip install opencv-python
#!pip install requests
#!pip install torch==2.3.1 torchvision==0.18.1 torchaudio==2.3.1 --index-url https://download.pytorch.org/whl/cu118
#!cd ../yolov7 && pip install -r requirements.txt


import subprocess
import json
import requests
import os
import torch
import cv2
import uuid
import io
import numpy as np
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, firestore, db, storage

# Get current working directory
server_dir = os.getcwd()

# Folder to store traffic camera images
traffic_images_path = os.path.join(server_dir, 'traffic_images')
os.makedirs(traffic_images_path, exist_ok=True)

damage_images_path = 'images/' # Path inside Firebase storage

# Initialize the Firebase Admin SDK with your service account key
cred = credentials.Certificate('test-cb666-firebase-adminsdk-k0oe5-7c992f8037.json')
firebase_admin.initialize_app(cred, {'databaseURL': 'https://test-cb666-default-rtdb.firebaseio.com/',
                                    "storageBucket": "test-cb666.appspot.com"})

# Reference to the database
ref = db.reference('Detections')

def detect_damage(image):
    global labels, data, damages, model

    # Preprocessing 1) Convert to RGB (required by YOLOv7), 2) resize to 640
    resized = cv2.resize(image, (640, 640))
    rgb = cv2.cvtColor(resized, cv2.COLOR_BGR2RGB)
    
    # Run the model to get predictions
    results = model(rgb)

    # Extract predictions (xmin, ymin, xmax, ymax, confidence, class)
    predictions = results.xyxy[0].cpu().numpy()
    
    # Get bbox coordinates, conf_score and class indexes
    for pred in predictions:
        xmin, ymin, xmax, ymax, conf, class_id = pred

        if conf >= MIN_CONF:
            dmg = resized.copy()
            description = labels[int(class_id)]

            # Draw bounding box
            cv2.rectangle(dmg, (int(xmin), int(ymin)), (int(xmax), int(ymax)), (0, 255, 0), 1)
            cv2.putText(dmg, description, (int(xmin), int(ymin) - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2)

            img_name = upload_image_to_storage(dmg, f'{damage_images_path}/{uuid.uuid1()}.jpg')
            #img_name = f'{uuid.uuid1()}.jpg'
            #img_path = os.path.join(traffic_images_path, img_name)
            #cv2.imwrite(img_path, dmg)
            
            data.append({
                'image':img_name,
                'latitude':latitude,
                'longitude':longitude,
                'date_of_detection':str(datetime.now().strftime("%Y/%m/%d %H:%M:%S")),
                'description':description,
            })
            
            damages+=1


def upload_image_to_storage(image, storage_path):
    # Point to Firebase Storage bucket
    bucket = storage.bucket()
    blob = bucket.blob(storage_path)

    # Encode image to a memory buffer
    _, buffer = cv2.imencode('.jpg', image)
    image_stream = io.BytesIO(buffer)

    # Upload the image from the memory buffer
    blob.upload_from_file(image_stream, content_type='image/jpeg')

    # Make the image publicly accessible
    blob.make_public()

    return blob.public_url

# Define the curl command as a list
curl_command = ['curl', 
                '--request', 
                'GET', 
                '--url', 
                'https://api.data.gov.sg/v1/transport/traffic-images']

# Run the curl command to retrieve camera data
result = subprocess.run(curl_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Convert the result to string
output = result.stdout.decode('utf-8')

parsed_output = json.loads(output)

# Get camera images and their metadata (Location, Camera_ID, Dimensions)
parsed_output = parsed_output['items'][0]['cameras']

# Only retrieve 1080p images, otherwise resolution too poor for road damages to be picked up
parsed_output = [data for data in parsed_output if (data['image_metadata']['height'] >= 1080) and (data['image_metadata']['width'] >= 1920)]
images = len(parsed_output)

downloaded = damages = failed = 0
MIN_CONF = 0.3
data = []

# Types of damages
labels = ['alligator crack', 
          'block crack', 
          'damaged base crack', 
          'localise surface defect', 
          'multi crack', 
          'peel off with cracks', 
          'peeling off premix', 
          'pothole with crack', 
          'rigid pavement crack', 
          'single crack', 
          'transverse crack', 
          'wearing course peeling off']

# Import trained model for road damage detection
model = torch.hub.load('../yolov7', 'custom', 'best.pt', source='local')

for img in parsed_output:
    # Send a GET request to fetch the image content
    response = requests.get(img['image'])
    latitude = img['location']['latitude']
    longitude = img['location']['longitude']
    
    # Check if the request was successful
    if response.status_code == 200:
        image_data = np.frombuffer(response.content, np.uint8)
        image = cv2.imdecode(image_data, cv2.IMREAD_COLOR)

        detect_damage(image)
                
        downloaded+=1
        
    else:
        failed+=1

ref.set(data) # Write data to firebase realtime database
print(f'{downloaded}/{images} images downloaded, {failed} failed, {damages} damages detected.')