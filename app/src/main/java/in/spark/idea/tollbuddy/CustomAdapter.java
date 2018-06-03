package in.spark.idea.tollbuddy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kanagasabapathi on 10/11/2017.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<DataModel> dataSet;
    private Context mCtx;
    private String mJourneyType;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView lblplazaname;
        TextView lbltollprice;
//        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.lblplazaname = (TextView) itemView.findViewById(R.id.lblplazaname);
            this.lbltollprice = (TextView) itemView.findViewById(R.id.lbltollprice);
//            this.imageViewIcon = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public CustomAdapter(Context ctx, ArrayList<DataModel> data, String JourneyType) {
        this.dataSet = data;
        this.mCtx = ctx;
        this.mJourneyType = JourneyType;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plaza_det, parent, false);

        view.setOnClickListener(TollResults.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView textViewName = holder.lblplazaname;
        TextView textViewVersion = holder.lbltollprice;
//        ImageView imageView = holder.imageViewIcon;

        textViewName.setText(dataSet.get(listPosition).getName());
        textViewVersion.setText( mJourneyType + " - " + mCtx.getString(R.string.Rs) + " " + dataSet.get(listPosition).getprice());
//        imageView.setImageResource(dataSet.get(listPosition).getImage());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}