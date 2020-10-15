package com.dominikp.mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.Comment;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context mContext;
    private List<Comment> mComments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.mContext = context;
        this.mComments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = mComments.get(position);

        holder.authorTextView.setText(comment.getAuthor());
        holder.createdAtTextView.setText(comment.getCreatedAt());
        holder.textTextView.setText(comment.getText());

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView authorTextView;
        private TextView createdAtTextView;
        private TextView textTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            authorTextView = itemView.findViewById(R.id.commentAuthorTextView);
            createdAtTextView = itemView.findViewById(R.id.commentCreatedAtTextView);
            textTextView = itemView.findViewById(R.id.commentTextView);

        }
    }
}
