package in.spark.idea.tollbuddy;

/**
 * Created by Kanagasabapathi on 10/11/2017.
 */

public class DataModel {
    String name;
    String price;
    int id_;
    int image;

    public DataModel(String name, String price, int id_, int image) {
        this.name = name;
        this.price = price;
        this.id_ = id_;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getprice() {
        return price;
    }

    public int getImage() {
        return image;
    }

    public int getId() {
        return id_;
    }
}
