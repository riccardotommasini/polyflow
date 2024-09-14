package org.streamreasoning.polyflow.api.sds.timevarying;

import org.streamreasoning.polyflow.api.operators.dag.DAGNode;

public class LazyTimeVarying<R extends Iterable<?>> implements TimeVarying<R> {

    DAGNode<R> dag;
    R content;

    String name;

    public LazyTimeVarying(DAGNode<R> dag, String name){
        this.dag = dag;
        this.name = name;
    }

    @Override
    public void materialize(long ts) {
        content = dag.eval(ts);
        if(content == null){
            throw new RuntimeException("Result of DAG computation is null");
        }
    }

    @Override
    public R get() {
        return content;
    }

    @Override
    public String iri() {
        return name;
    }

    @Override
    public boolean named() {
        return true;
    }
}
