class WebView extends WebObserver{
    static #location = { lat: 1.3578249748956959, lng: 103.83685903179386 };	// LTA Sin Ming Office
    static #map;
    static #table;
    static #markers = [];

    static async init(){
        // Redirect to login page if user is not logged in (Dashboard-specific logic)
        auth.onAuthStateChanged((user) => {
            if (!user && window.location.pathname.endsWith('dashboardUI.html')) {
                window.location.href = "loginUI.html";
            }
        });

        // Initialise dashboard clock display
        setInterval(WebView.updateClock, 1000);
        WebView.updateClock();

        WebObserver.dbCases = await db.ref('Cases');
        WebObserver.dbReports = await db.ref('Reports');
        WebObserver.dbDetections = await db.ref('Detections');

        // Listen for value changes at this reference
        await WebObserver.dbDetections.on('value', (snapshot) => {
            WebView.updateCount('detectionButton');
            alert("Detections database updated");
        });

        await WebObserver.dbReports.on('value', (snapshot) => {
            WebView.updateCount('reportButton');
            alert("Reports database updated");
        });

        await WebObserver.dbCases.on('value', (snapshot) => {
            WebView.updateCount('caseButton');
            alert("Cases database updated");
        });
    
        // Display cases database upon opening dashboard
        document.getElementById('caseButton').click();
        document.getElementById('apply').click();
    }

    static updateClock() {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        const timeString = `${hours}:${minutes}:${seconds}`;
        document.getElementById('clock').textContent = timeString;
    }

    static async updateDisplay(readerObj){
        this.drawMap(readerObj);
        this.drawTable(readerObj);   
    }

    static highlightButton(element){
        document.querySelectorAll("button").forEach((button) => {
            button.classList.remove('btn-warning');
        });

        document.getElementById(element).classList.add('btn-warning');
    }

    static async updateCount(element){
        const reader = ObjectFactory.createReader(element);
        const count = await reader.get_data();
        document.getElementById(element).textContent = document.getElementById(element).textContent.split(": ")[0] + ": " + count.length;

        return count.length;
    }

    static async drawMap(dbType) {
        // Center map on location 
        this.#map = new google.maps.Map(document.getElementById("map"), {
            zoom: 10,
            center: this.#location,
            mapTypeId: google.maps.MapTypeId.SATELLITE
        });

        const data = await dbType.get_data();
    
        data.forEach(marker=>{
            if (marker['status'] != 'Repair Completed'){
                const mkr = new google.maps.Marker({
                    position: { lat: parseFloat(marker['latitude']), lng: parseFloat(marker['longitude'])},
                    map: this.#map,
                    icon: {
                        url: "http://maps.google.com/mapfiles/ms/icons/red-dot.png", // Default red marker icon
                        scaledSize: new google.maps.Size(30, 30), // Set the size of the marker
                    },
                    id: marker['image']
                });
        
                this.#markers.push(mkr);  // Push newly created marker into the buffer
        
                // Add listener to 1) display image of the damage and 2) highlight the row in the table when i click on this marker
                mkr.addListener("click", function() {
                    WebView.focus(mkr.id);
                });
            }
        });
    }
    
    static async drawTable(dbType) {
        this.#table = document.getElementById('caseTable'); 
        // Delete old table
        this.#table.innerHTML = "";
    
        // Create new table
        const thead = document.createElement('thead');  // Create table header
        const tbody = document.createElement('tbody'); // Create table body
        const row = document.createElement('tr'); // Create row for header

        // Get headers and data
        const headers = dbType.get_header();
        const status_options = dbType.get_status();
        const data = await dbType.get_data();
    
        headers.forEach(headerText => {
            const th = document.createElement('th');    // Create cell for each header
            th.textContent = headerText;
            row.appendChild(th);
        });
    
        thead.appendChild(row);
        this.#table.appendChild(thead);
    
        data.forEach(entry => {
            // Create row for each entry
            const row = document.createElement('tr');   
            for(const key in entry) {
                // Create cell for each row
                const td = document.createElement('td');
                // For Date of Repair cell   
                switch (key){
                    case 'date_of_repair':
                        const input = document.createElement('input');
                        input.type = 'date';
                        input.value = entry[key];
        
                        input.addEventListener('change', function () {
                            input.value = this.value;
                        });
        
                        td.appendChild(input);
                        break;
                    case 'status':
                        const select = document.createElement('select');
                        const selectedValue = document.createElement('span'); // Shows the selected value
                        
                        // Create and append the predefined options to the dropdown menu
                        status_options.forEach(val => {
                            const option = document.createElement('option');
                            option.value = val;
                            option.textContent = val;
                            select.appendChild(option);
                        });
        
                        select.value = entry[key];
        
                        select.addEventListener('change', function () {
                            selectedValue.textContent = this.value;
                        });
                        
                        td.appendChild(select);
                        break;
                    default:
                        td.textContent = entry[key];
                }
                row.appendChild(td);
            }
            // Add listener to 1) highlight row as active and 2) display image when i click on this row
            row.addEventListener('click', function () {
                WebView.focus(entry['image']);
            });
    
            tbody.appendChild(row);
        });
     
        this.#table.appendChild(tbody);
    }

    // When a case/marker is clicked, 1) highlight its row, 2) highlight its marker, 3) display its image 
    static focus(imgName){
        const imageContainer = document.getElementById('imageContainer');
        imageContainer.innerHTML = `<img src="${imgName}" alt="${imgName}"/>`;

        const rows = document.querySelectorAll('tbody tr');
        rows.forEach(r => {
            if (r.cells[1].textContent == imgName){
                r.classList.add('table-primary');
            }
            else{
                r.classList.remove('table-primary');
            }
        });

        this.#markers.forEach(marker => {
            if (marker.id == imgName){
                marker.setIcon({
                    url: "http://maps.google.com/mapfiles/ms/icons/green-dot.png", // Change to green
                    scaledSize: new google.maps.Size(30, 30), // Size of the marker
                });
            }
            else{
                marker.setIcon({
                    url: "http://maps.google.com/mapfiles/ms/icons/red-dot.png", // Default red marker icon
                    scaledSize: new google.maps.Size(30, 30), // Set the size of the marker
                });
            }
        });
    }
}







