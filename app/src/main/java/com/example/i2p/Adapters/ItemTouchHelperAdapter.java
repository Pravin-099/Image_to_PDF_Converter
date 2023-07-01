package com.example.i2p.Adapters;

public interface ItemTouchHelperAdapter {

    // Called when an item is moved in the RecyclerView
    void onItemMove(int fromPosition, int toPosition);

    // Called when an item is dismissed (swiped) in the RecyclerView
    void onItemDismiss(int position);
}
