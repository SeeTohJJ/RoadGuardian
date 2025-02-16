// Initialise Firebase
const firebaseConfig = {
	apiKey: "AIzaSyA2KOmVN48MvlJ3qwFuCwxiqICTRyAQxQg",
	authDomain: "test-cb666.firebaseapp.com",
	databaseURL: "https://test-cb666-default-rtdb.firebaseio.com",
	projectId: "test-cb666",
	storageBucket: "test-cb666.appspot.com",
	messagingSenderId: "958198464791",
	appId: "1:958198464791:web:dd483f7f8aa662f8803752",
	measurementId: "G-FRXXNZC15L"
};

const app = firebase.initializeApp(firebaseConfig);
const db = firebase.database();
const auth = firebase.auth();

class DatabaseReader{
	// These shouldn't be called
	get_header(){
		alert("Abstract get_header() called!");
	}

	get_status(){
		alert("Abstract get_status() called!");	
	}

	async get_data(){
		alert("Abstract get_data() called!");
	}
}

class CaseReader extends DatabaseReader{
	get_header(){
		return ['ID','Image','Latitude','Longitude','Description','Date of Occurrence','Date of Repair','Status'];
	}

	get_status(){
		return ['Pending Repair','Repair Completed','Delete'];
	}
	// Overriding
	async get_data(){
		const dbRef = WebView.dbCases;
		const data = [];

		await dbRef.once('value')
			.then((snapshot) => {
			if (snapshot.exists()) {
				const dataset = snapshot.val();
				const records = Object.entries(dataset).map(([key, user]) => ({
					id: key,
					image: user.image,
					latitude: user.latitude,
					longitude: user.longitude,
					description: user.description,
					date_of_occurrence: user.date_of_occurrence,
					date_of_repair: user.date_of_repair,
					status: user.status
				}));
				
				records.forEach(record => {
					data.push({
						'id': record.id,
						'image': record.image,
						'latitude': record.latitude,
						'longitude': record.longitude,
						'description': record.description,
						'date_of_occurrence': record.date_of_occurrence,
						'date_of_repair': record.date_of_repair,
						'status': record.status
					});
				});
			}
		});

		return data;
	}
}

class ReportReader extends DatabaseReader{
	get_header(){
		return ['ID','Image','Latitude','Longitude','Description','Date of Report','Action'];
	}

	get_status(){
		return ['Pending Review','Add Case','Dismiss'];
	}
	// Overriding
	async get_data(){
		const dbRef = WebView.dbReports;
		const data = [];

		await dbRef.once('value')
			.then((snapshot) => {
			if (snapshot.exists()) {
				const dataset = snapshot.val();
				
				const records = Object.entries(dataset).map(([key, user]) => ({
					id: key,
					image: user.Photo,
					latitude: user.Latitude,
					longitude: user.Longitude,
					description: user.Description,
					date_of_report: user.Timestamp,
				}));

				records.forEach(record => {
					data.push({
						'id': record.id,
						'image': record.image,
						'latitude': record.latitude,
						'longitude': record.longitude,
						'description': record.description,
						'date_of_report': record.date_of_report,
						'status': '-'
					});
				});
			}
		});

		return data;
	}
}

class DetectionReader extends DatabaseReader{
	get_header(){
		return ['ID','Image','Latitude','Longitude','Description','Date of Detection','Action'];
	}

	get_status(){
		return ['Pending Review','Add Case','Dismiss'];
	}
	// Overriding
	async get_data(){
		const dbRef = WebView.dbDetections;
		const data = [];

		await dbRef.once('value')
			.then((snapshot) => {
			if (snapshot.exists()) {
				const dataset = snapshot.val();
				const records = Object.entries(dataset).map(([key, user]) => ({
					id: key,
					image: user.image,
					latitude: user.latitude,
					longitude: user.longitude,
					description: user.description,
					date_of_detection: user.date_of_detection,
				}));

				records.forEach(record => {
					data.push({
						'id': record.id,
						'image': record.image,
						'latitude': record.latitude,
						'longitude': record.longitude,
						'description': record.description,
						'date_of_detection': record.date_of_detection,
						'status': '-'
					});
				});
			}
		});

		return data;
	}
}

class AdminReader extends DatabaseReader{
	#email;
	#password;

	constructor(email, password){
		super();
		this.#email = email;
		this.#password = password;
	}
	
	async get_data(){
		let login = false;

		await auth.signInWithEmailAndPassword(this.#email, this.#password)
		.then((userCredential) => {
			//alert('Welcome');
			login = true;
		})
		.catch((error) => {
			//alert('Invalid Credentials');
			return false;
		});

		const dbRef = await db.ref('Admin');
		
		await dbRef.orderByChild('email').equalTo(this.#email).once("value", snapshot => {
			if (snapshot.exists()) {
				//alert("Found");
			}
			else{
				alert("You are not admin!");
				login = false;
			}
		})
		.catch(error=>{
			alert("Admin database empty");
			login = false;
		});


		return login;
    }
}