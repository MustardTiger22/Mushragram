package com.dominikp.mobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.PlaceOverview;
import com.squareup.picasso.Picasso;
import java.util.List;

public class PlaceOverviewAdapter extends RecyclerView.Adapter<PlaceOverviewAdapter.PlaceOverviewHolder> {
    private Context mContext;
    private List<PlaceOverview> mPlaces;
    private OnItemClickListener mListener;

    private int[] gobletArray = new int[] {
            R.drawable.ic_gold_goblet,
            R.drawable.ic_silver_goblet,
            R.drawable.ic_brown_goblet};

    public PlaceOverviewAdapter(Context context, List<PlaceOverview> places) {
        this.mContext = context;
        this.mPlaces = places;
    }

    @NonNull
    @Override
    public PlaceOverviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.place_item, parent, false);
        return new PlaceOverviewAdapter.PlaceOverviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceOverviewHolder holder, int position) {
        PlaceOverview place = mPlaces.get(position);

        Picasso.get()
                .load(place.getImageUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.placeImage);

        holder.displayName.setText(place.getDisplayName());

        holder.gobletImage.setImageResource(gobletArray[position]);
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public class PlaceOverviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView placeImage;
        private TextView displayName;
        private ImageView gobletImage;

        public PlaceOverviewHolder(@NonNull View itemView) {
            super(itemView);

            placeImage = itemView.findViewById(R.id.imageViewPlaceImage);
            displayName = itemView.findViewById(R.id.placeUsernameTextView);
            gobletImage = itemView.findViewById(R.id.imageViewPlaceGoblet);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(mListener != null) {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
