package net.kilogram.messenger.config.cell;

import androidx.recyclerview.widget.RecyclerView;

import net.kilogram.messenger.config.CellGroup;

public abstract class AbstractConfigCell {
    // can not be null!
    protected CellGroup cellGroup;

    // called by CellGroup.java
    public void bindCellGroup(CellGroup cellGroup) {
        this.cellGroup = cellGroup;
    }

    public abstract int getType();

    public abstract boolean isEnabled();

    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder);
}