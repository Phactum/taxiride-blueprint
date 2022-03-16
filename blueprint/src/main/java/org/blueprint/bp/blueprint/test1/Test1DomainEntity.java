package org.blueprint.bp.blueprint.test1;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.blueprint.bp.blueprint.config.ApplicationProperties;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

@Entity
@Table(name = "TEST1")
public class Test1DomainEntity extends WorkflowDomainEntity {

    @Lob
    @Column(name = "ITEM_IDS", columnDefinition = "BLOB")
    @Basic(fetch = FetchType.EAGER)
    @ElementCollection
    private List<String> itemIds;

    @Override
    public String getWorkflowModuleId() {

        return ApplicationProperties.WORKFLOW_MODULE_ID;

    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

}
