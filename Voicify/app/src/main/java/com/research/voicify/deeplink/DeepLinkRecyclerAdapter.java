package com.research.voicify.deeplink;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.research.voicify.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DeepLinkRecyclerAdapter extends RecyclerView.Adapter<DeepLinkRecyclerAdapter.ViewHolder> {
    private final MyClickListener listener;
    ArrayList<DeepLinkItem> data = new ArrayList<>();

    /**
     * This is the constructor of the recycler view, call this to create one
     * @param data : the initialized data for the list
     * @param listener : onclick listener
     */
    public DeepLinkRecyclerAdapter(ArrayList<DeepLinkItem> data,MyClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeepLinkRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.deeplink_item, parent, false);
        return new ViewHolder(v, listener);  // this function create and return a view holder
    }


    /**
     * This function is used for inflating data inside an item in the list
     * @param holder: each of a item
     * @param position: the location of the item in the list for data retrieval
     */
    @Override
    public void onBindViewHolder(@NonNull DeepLinkRecyclerAdapter.ViewHolder holder, int position) {
        holder.title.setText(data.get(position).title);     //
        String concatenateCommand = "";
        for (String command : data.get(position).commands) {
            concatenateCommand += command + " > ";
        }
        holder.commands.setText(concatenateCommand);

    }

    /**
     * This function will immediately sync the data with the external data from activity.
     * @param data: list of item
     */
    public void updateData(ArrayList<DeepLinkItem> data){
        this.data = data;
        notifyDataSetChanged();
    }

    /**
     * This function will return the number of item within the list of item provided for this list.
     * @return item count
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title;
        public TextView commands;
        public ImageButton deleteBtn;

        MyClickListener listener;

        public ViewHolder(@NonNull View itemView, MyClickListener listener) {
            super(itemView);
            this.listener = listener;

            title = itemView.findViewById(R.id.itemTitle);
            commands = itemView.findViewById(R.id.itemCommand);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            title.setOnClickListener(this);
            deleteBtn.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.itemTitle:
                    listener.onEdit(this.getLayoutPosition());
                    break;
                case R.id.deleteBtn:
                    listener.onDelete(this.getLayoutPosition());
                    break;
                default:
                    break;
            }
        }
    }
    public interface MyClickListener {
        void onEdit(int p);
        void onDelete(int p);
    }
}
