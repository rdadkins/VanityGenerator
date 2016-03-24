package co.bitsquared.vanitygenerator.android.query;

import android.os.Parcel;
import android.os.Parcelable;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.Query;

/**
 * ParcelableQuery is an extension of Query that allows for Query's to be 'parsed' in Android.
 * @see co.bitsquared.vanitygenerator.core.query.Query
 */
public class ParcelableQuery extends Query implements Parcelable {

    private static final int BOOLEAN_ARRAY_SIZE = 5;

    public ParcelableQuery(QueryBuilder builder) {
        super(builder);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getPlainQuery());
        dest.writeValue(isBegins());
        dest.writeBooleanArray(new boolean[]{isBegins(), isMatchCase(), isCompressed(), isFindUnlimited(), isP2SH()});
        dest.writeSerializable(getNetworkParameters(null));
    }

    public static final Parcelable.Creator<ParcelableQuery> CREATOR = new Parcelable.Creator<ParcelableQuery>() {

        public ParcelableQuery createFromParcel(Parcel source) {
            QueryBuilder queryBuilder = new QueryBuilder(source.readString());
            boolean[] params = new boolean[BOOLEAN_ARRAY_SIZE];
            source.readBooleanArray(params);
            queryBuilder.begins(params[0]);
            queryBuilder.matchCase(params[1]);
            queryBuilder.compressed(params[2]);
            queryBuilder.findUnlimited(params[3]);
            queryBuilder.searchForP2SH(params[4]);
            queryBuilder.targetNetwork((GlobalNetParams) source.readSerializable());
            return new ParcelableQuery(queryBuilder);
        }

        public ParcelableQuery[] newArray(int size) {
            return new ParcelableQuery[0];
        }

    };

}
