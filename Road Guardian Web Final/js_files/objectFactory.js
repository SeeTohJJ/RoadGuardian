// Factory
class ObjectFactory{
    static createReader(element){
        switch (element){
            case 'caseButton':
                return new CaseReader();
            case 'reportButton':
                return new ReportReader();
            case 'detectionButton':
                return new DetectionReader();
            default:
                return null;
        }  
    }

    static createEditor(element){
        switch (element){
            case 'caseButton':
                return new CaseEditor();
            case 'reportButton':
                return new ReportEditor();
            case 'detectionButton':
                return new DetectionEditor();
            default:
                return null;
        }  
    }

    static createAuth(element, email, password){
        switch(element){
            case 'loginButton':
                return new AdminReader(email, password);
            case 'registerButton':
                return new AdminEditor(email, password);
            case 'resetButton':
                return new AdminEditor(email, null);
            default:
                return null;
        }
    }
}