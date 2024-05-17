/*
package org.streamreasoning.rsp4j.api.sds.timevarying;

import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.sds.SDS;

public class LazyTimeVarying<R extends Iterable<?>> implements TimeVarying<R> {

    SDS<R> sds;
    DAG<R> dag;
    R content;

    String name;
    public LazyTimeVarying(SDS<R> sds, DAG<R> dag){
        this.sds = sds;
        this.dag = dag;
    }

    @Override
    public void materialize(long ts) {
        this.sds.materialize(ts);
        for(TimeVarying<R> tvg : sds.asTimeVaryingEs()){
            dag.prepare(tvg.iri(), tvg.get());
        }
        content = dag.eval();
        dag.clear();
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
    public void setIri(String name) {
        this.name = name;
    }

    @Override
    public boolean named() {
        return true;
    }
}
*/
