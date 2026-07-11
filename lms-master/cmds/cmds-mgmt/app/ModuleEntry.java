import com.vedantu.commons.pojos.SrcEntity;

public class ModuleEntry {

    public SrcEntity entity;
    public boolean   isCompletable;
    public int       completionRule;

    @Override
    public boolean equals(Object o) {

        if (null == o || !(o instanceof SrcEntity)) {
            return false;
        }
        ModuleEntry e = (ModuleEntry) o;
        return entity != null && entity == e.entity;
    }

    @Override
    public int hashCode() {
        return entity == null ? 0 : (entity.type.name() + ":" + entity.id).hashCode();
    }
}