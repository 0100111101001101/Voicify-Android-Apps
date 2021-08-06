package com.research.voicify.GoogleNLU;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.VisibleForTesting;

import com.google.api.services.language.v1.model.Entity;

import java.util.Map;

public class EntityModel implements Parcelable {
    @VisibleForTesting
    static final String KEY_WIKIPEDIA_URL = "wikipedia_url";

    /**
     * The representative name for the entity.
     */
    public final String name;

    /**
     * The entity type.
     */
    public final String type;

    /**
     * The salience score associated with the entity in the [0, 1.0] range.
     */
    public final float salience;

    /**
     * The Wikipedia URL.
     */
    public final String wikipediaUrl;
    protected EntityModel(Parcel in) {
        name = in.readString();
        type = in.readString();
        salience = in.readFloat();
        wikipediaUrl = in.readString();
    }

    public static final Creator<EntityModel> CREATOR = new Creator<EntityModel>() {
        @Override
        public EntityModel createFromParcel(Parcel in) {
            return new EntityModel(in);
        }

        @Override
        public EntityModel[] newArray(int size) {
            return new EntityModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeFloat(salience);
        dest.writeString(wikipediaUrl);
    }


    public EntityModel(Entity entity) {
        name = entity.getName();
        type = entity.getType();
        salience = entity.getSalience();
        final Map<String, String> metadata = entity.getMetadata();
        if (metadata != null && metadata.containsKey(KEY_WIKIPEDIA_URL)) {
            wikipediaUrl = metadata.get(KEY_WIKIPEDIA_URL);
        } else {
            wikipediaUrl = null;
        }
    }
}
