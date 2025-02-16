class DatabaseEditor{
    async update(){
        alert("Abstract update() called");
    }
}

class CaseEditor extends DatabaseEditor{
    async update(){
        const dbCase = WebController.dbCases;

        // Check if Cases database exists
        dbCase.once('value')
        .then((snapshot) => {
            if (!snapshot.exists()) {
                alert('No existing cases');
                return;
            }
        });

        const table = document.getElementById('caseTable');
        const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

        //alert("Refreshing Cases Table");
        for (let i=0;i<rows.length;i++){
            const cells = rows[i].getElementsByTagName('td');
            const dateInput = cells[6].querySelector('input');
            const select = cells[7].querySelector('select');
            const selectedValue = select.value; 
            
            if (selectedValue == 'Delete'){
                dbCase.child(cells[0].textContent).remove();
            }
            else{
                dbCase.child(cells[0].textContent).update({
                    date_of_repair: dateInput.value,
                    status: selectedValue
                });
            }
        }
    }
}

class ReportEditor extends DatabaseEditor{
    async update(){
        const dbReport = WebController.dbReports;
        const dbCase = WebController.dbCases;
        const table = document.getElementById('caseTable');
        const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

        //alert("Refreshing Reports Table");
        for (let i=0;i<rows.length;i++){
            const cells = rows[i].getElementsByTagName('td');
            const select = cells[6].querySelector('select');
            const selectedValue = select.value; 
            
            if (selectedValue == 'Add Case'){
                dbCase.push({
                    image: cells[1].textContent,
                    latitude: cells[2].textContent,
                    longitude: cells[3].textContent,
                    description: cells[4].textContent,
                    date_of_occurrence: cells[5].textContent,
                    date_of_repair: '-',
                    status: 'Pending Repair'
                });
                dbReport.child(cells[0].textContent).remove();
                
            }
            else if (selectedValue == 'Dismiss'){
                dbReport.child(cells[0].textContent).remove();
            }
        }
    }
}

class DetectionEditor extends DatabaseEditor{
    async update(){
        const dbDetection = WebController.dbDetections;
        const dbCase = WebController.dbCases;
        const table = document.getElementById('caseTable');
        const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

        //alert("Refreshing Detections Table");
        for (let i=0;i<rows.length;i++){
            const cells = rows[i].getElementsByTagName('td');
            const select = cells[6].querySelector('select');
            const selectedValue = select.value; 
 
            if (selectedValue == 'Add Case'){
                dbCase.push({
                    image: cells[1].textContent,
					latitude: cells[2].textContent,
					longitude: cells[3].textContent,
					description: cells[4].textContent,
					date_of_occurrence: cells[5].textContent,
					date_of_repair: '-',
					status: 'Pending Repair'
                });
                dbDetection.child(cells[0].textContent).remove();
            }
            else if (selectedValue == 'Dismiss'){
                dbDetection.child(cells[0].textContent).remove();
            }
        }
    }
}

class AdminEditor extends DatabaseEditor{
    #email;
	#password;
    
    constructor(email, password){
        super();
        this.#email = email;
        this.#password = password;
    }

    async update(){
        let success = false;
        const dbAdmin = await db.ref('Admin');

        if (this.#password){ // Register Account
            await auth.createUserWithEmailAndPassword(this.#email, this.#password)
            .then((userCredential) => {
                success = true;
                dbAdmin.push({email:this.#email});
            })
            .catch((error) => {
                //alert('Invalid Credentials');
            });
        }
        else{   // Reset Password
            await auth.sendPasswordResetEmail(this.#email)
            .then(() => {
                success = true;
            })
            .catch((error) => {
                //alert('Invalid Credentials');
            });
        }
        
        return success;    
    }
}