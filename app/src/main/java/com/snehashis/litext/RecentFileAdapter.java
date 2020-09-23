package com.snehashis.litext;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecentFileAdapter extends RecyclerView.Adapter<RecentFileAdapter.ViewHolder> {

    private ArrayList<RecentFile> recentFiles;
    ItemClicked activity;

    public interface ItemClicked{
        void onItemCLicked(int index);
    }

    public RecentFileAdapter(Context context, ArrayList<RecentFile> list) {
        recentFiles = list;
        activity = (ItemClicked)context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView fileType;
        TextView fileName, filePath;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileType = itemView.findViewById(R.id.fileType);
            fileName = itemView.findViewById(R.id.fileName);
            filePath = itemView.findViewById(R.id.filePath);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemCLicked(recentFiles.indexOf((RecentFile) v.getTag()));
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
            });

        }
    }

    @NonNull
    @Override
    public RecentFileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_files, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentFileAdapter.ViewHolder holder, int position) {

        holder.itemView.setTag(recentFiles.get(position));

        holder.fileName.setText(recentFiles.get(position).getFileName());
        holder.filePath.setText(recentFiles.get(position).getFilePath());
        ArrayList<String> codingExtensions = new ArrayList<>();

        //Adding some file extensions that will show a different image view
        codingExtensions.add("java");
        codingExtensions.add("c");
        codingExtensions.add("cpp");
        codingExtensions.add("py");
        codingExtensions.add("js");
        codingExtensions.add("html");
        codingExtensions.add("css");
        codingExtensions.add("xml");
        codingExtensions.add("json");
        codingExtensions.add("sh");

        if(codingExtensions.contains(recentFiles.get(position).getFileType()))
            holder.fileType.setImageResource(R.drawable.code);
        else
            holder.fileType.setImageResource(R.drawable.file);
    }

    @Override
    public int getItemCount() {

        return recentFiles!=null?recentFiles.size():0;
    }
}
