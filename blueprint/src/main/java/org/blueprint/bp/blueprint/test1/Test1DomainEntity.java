package org.blueprint.bp.blueprint.test1;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "TEST1")
public class Test1DomainEntity {

    @Id
    @Column(name = "ID", columnDefinition = "VARCHAR(40)")
    private String id;

    @Lob
    @Column(name = "ITEM_IDS", columnDefinition = "BLOB")
    @Basic(fetch = FetchType.EAGER)
    @ElementCollection
    private List<String> itemIds;

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
