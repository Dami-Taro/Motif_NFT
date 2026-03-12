package src.graph;

public class GraphException extends RuntimeException{
    public GraphException(String message){
        super(message);
        printStackTrace();
    }
}
