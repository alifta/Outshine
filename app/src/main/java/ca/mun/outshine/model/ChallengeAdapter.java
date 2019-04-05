package ca.mun.outshine.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ca.mun.outshine.R;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private final Context context;
    private ArrayList<Challenge> challengeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int postion);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {

        // widgets
        public ImageView imageView;
        public TextView challengeName;
        public TextView challengeStart;
        public TextView challengeEnd;

        // define UI elements here
        public ChallengeViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.challenge_image);
            challengeName = itemView.findViewById(R.id.challenge_name);
            challengeStart = itemView.findViewById(R.id.challenge_start);
            challengeEnd = itemView.findViewById(R.id.challenge_end);
            // config item view listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

//    public ChallengeAdapter(ArrayList<Challenge> challengeList) {
//        this.challengeList = challengeList;
//    }

    public ChallengeAdapter(Context context, ArrayList<Challenge> challengeList) {
        this.context = context;
        this.challengeList = challengeList;
    }

    // pass layout to adaptor
    @Override
    public ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_challenge, parent, false);
        ChallengeViewHolder viewHolder = new ChallengeViewHolder(view, listener);
        return viewHolder;
    }

    // pass on information from list entered via constructor
    @Override
    public void onBindViewHolder(ChallengeViewHolder holder, int position) {
        Challenge currentItem = challengeList.get(position);
        // set value of UI elements
        holder.imageView.setImageResource(currentItem.getImageResource());
        holder.challengeName.setText(currentItem.getName());
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm aaa");
        holder.challengeStart.setText("Started: " + dateFormat.format(currentItem.getTime_starts()));
        holder.challengeEnd.setText("Ends: " + dateFormat.format(currentItem.getTime_ends()));
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }
}
