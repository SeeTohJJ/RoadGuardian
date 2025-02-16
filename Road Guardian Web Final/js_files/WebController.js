// Context
class WebController extends WebObserver{
    static #readerObj;
    static #editorObj;

    static async viewChange(element){
        WebView.highlightButton(element);
        this.#readerObj = ObjectFactory.createReader(element);
        this.#editorObj = ObjectFactory.createEditor(element);

        WebView.updateDisplay(this.#readerObj);
    }

    static async apply(){
        // Edit Data
        if (this.#editorObj){
            await this.#editorObj.update();
        }
        // Update Data
        if (this.#readerObj){
            WebView.updateDisplay(this.#readerObj);
        }
    }
    
    static async cancel(){
        if (this.#readerObj){
            WebView.updateDisplay(this.#readerObj);
        }   
    }
    
    static logout(){
        window.location.replace('loginUI.html');
        auth.signOut();
    }
}


