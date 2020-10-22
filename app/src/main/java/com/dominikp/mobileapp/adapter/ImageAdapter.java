package com.dominikp.mobileapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.StringJoiner;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mListener;

    public ImageAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);

        holder.textViewName.setText(uploadCurrent.getTitle());
        holder.textViewAuthor.setText(uploadCurrent.getAuthor());
        holder.textViewLikeCounter.setText(String.valueOf(uploadCurrent.getLikes().size()));

        if(uploadCurrent.getLocation() != null) {
            StringJoiner uploadLocation = new StringJoiner(", ");
            uploadLocation.add(uploadCurrent.getLocation().getCity());
            uploadLocation.add(uploadCurrent.getLocation().getCountry());

            holder.textViewLocation.setVisibility(View.VISIBLE);
            holder.textViewLocation.setText(uploadLocation.toString());
        }

        if(uploadCurrent.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.textViewAuthor.setTextColor(Color.rgb(28, 92, 7));
        }

        if(uploadCurrent.getLikes().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.imageHeart.setColorFilter(Color.RED);
        } else {
            holder.imageHeart.setColorFilter(Color.GRAY);
        }

        // Załadowanie obrazka do widoku
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {
        public TextView textViewName;
        public TextView textViewLikeCounter;
        public TextView textViewAuthor;
        public TextView textViewLocation;
        public ImageView imageView;
        public ImageView imageHeart;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.imageName);
            imageView = itemView.findViewById(R.id.imageViewUpload);
            imageHeart = itemView.findViewById(R.id.heartImageView);
            textViewAuthor = itemView.findViewById(R.id.authorTextView);
            textViewLikeCounter = itemView.findViewById(R.id.likeCounterTextView);
            textViewLocation = itemView.findViewById(R.id.locationTextView);

            itemView.setOnClickListener(this);
            imageHeart.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null) {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    switch (view.getId()) {
                        case R.id.heartImageView:
                            mListener.onPlusClick(position);
                            break;
                        default:
                            mListener.onItemClick(position);
                            break;
                    }
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem likePhoto;
            menu.setHeaderTitle("Co chcesz zrobic?");

            //Uzyskanie aktualnie wskazanego obiektu (wiersza) w adapterze
            int position = getLayoutPosition();
            Upload currentRow = mUploads.get(position);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(!currentRow.getLikes().containsKey(user.getUid())) {
                likePhoto = menu.add(Menu.NONE, 1, 1, "Polub");
            }
            else {
                likePhoto = menu.add(Menu.NONE, 1, 1, "Odlub");
            }

            //Sprawdzenie, czy dodany post należy do użytkownika
            if(currentRow.getUserId().equals(user.getUid())) {
                MenuItem delete = menu.add(Menu.NONE, 2, 2, "Usuń");
                delete.setOnMenuItemClickListener(this);
            }
            likePhoto.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener != null) {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    switch (item.getItemId()) {
                        case 1:
                            mListener.onLikeClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;

                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onPlusClick(int position);

        void onLikeClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
