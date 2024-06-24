package document.datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class DocumentCollection implements Iterable<String>{

    private List<String> jsonObjects = new ArrayList<>();

    public DocumentCollection(){}

    public void addElement(String jsonObject){
        this.jsonObjects.add(jsonObject);
    }

    public List<String> getJsonObjects(){
        return this.jsonObjects;
    }

    @Override
    public Iterator<String> iterator() {
        return this.jsonObjects.iterator();
    }

    @Override
    public void forEach(Consumer<? super String> action){
        jsonObjects.forEach(action);
    }

    public boolean isEmpty(){
        return this.jsonObjects.isEmpty();
    }

    public DocumentCollection append(DocumentCollection d2){
        DocumentCollection res = new DocumentCollection();
        jsonObjects.forEach(res::addElement);
        d2.forEach(res::addElement);
        return res;
    }
}
