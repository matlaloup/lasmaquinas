package org.mlaloup.lasmaquinas.activity.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.mlaloup.lasmaquinas.activity.ProjectsActivity;
import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.model.ClimberRef;
import org.mlaloup.lasmaquinas.model.TickList;

import java.io.IOException;


public class PreferencesHelper {

    private static final String TAG = "PreferencesHelper";

    public static final Gson GSON = createGson();

    public static class PreferencesHelperBuilder {

        private SharedPreferences prefs;


        public PreferencesHelperBuilder(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        public SharedPreferences get() {
            return prefs;
        }

        public void saveObject(String key, Object obj) {
            String json = GSON.toJson(obj);
            prefs.edit().putString(key, json).commit();
        }

        public <T> T getObject(String key, T defaultObj, Class<T> type) {
            try {
                String defaultJson = GSON.toJson(defaultObj);
                String json = prefs.getString(key, defaultJson);
                if (json == "") {
                    return null;
                }
                T result = GSON.fromJson(json, type);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Unable to  parse json object of type " + type + " from key " + key, e);
                return defaultObj;
            }
        }


    }

    public static PreferencesHelperBuilder privateSettings(Activity activity) {
        return new PreferencesHelperBuilder(activity.getPreferences(Context.MODE_PRIVATE));
    }

    public static PreferencesHelperBuilder globalSettings(Context context) {
        return new PreferencesHelperBuilder(PreferenceManager.getDefaultSharedPreferences(context));
    }


    private static Gson createGson() {
        return new GsonBuilder().registerTypeAdapterFactory(new LasMaquinasTypeAdapterFactory()).create();
    }

    /**
     * TypeAdapterFactory qui gère les problématiques de références circulaires en les breakant quand c'est nécessaire en transformant,
     * lors de la serialisation un Climber en ClimberRef, et à la deserialisation, en faisant l'inverse si c'est possible.
     */
    public static class LasMaquinasTypeAdapterFactory implements TypeAdapterFactory {


        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            if (type.getRawType().equals(Climber.class)) {
                return (TypeAdapter<T>) new ClimberTypeAdapter((TypeAdapter<Climber>) delegate);
            }
            if (type.getRawType().equals(TickList.class)) {
                return (TypeAdapter<T>) new TickListAdapter((TypeAdapter<TickList>) delegate);
            }
            return delegate;
        }
    }

    public static class ClimberTypeAdapter extends TypeAdapter<Climber> {

        TypeAdapter<Climber> delegate;

        public ClimberTypeAdapter(TypeAdapter<Climber> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Climber read(JsonReader in) throws IOException {
            Climber climber = delegate.read(in);
            if (climber == null) {
                return null;
            }
            TickList tickList = climber.getTickList();
            //on remet le lien bidirectionnel. Aisi, s'il s'agissait d'une simple climbreRef, on remet le climber complet.
            tickList.setClimber(climber);
            return climber;
        }

        @Override
        public void write(JsonWriter out, Climber value) throws IOException {
            delegate.write(out, value);
        }
    }


    public static class TickListAdapter extends TypeAdapter<TickList> {

        TypeAdapter<TickList> delegate;

        public TickListAdapter(TypeAdapter<TickList> delegate) {
            this.delegate = delegate;
        }

        @Override
        public TickList read(JsonReader in) throws IOException {
            TickList tickList = delegate.read(in);
            return tickList;
        }

        @Override
        public void write(JsonWriter out, TickList value) throws IOException {
            ClimberRef climber = value.getClimber();
            ClimberRef climberRef = climber;
            if (climberRef instanceof Climber) {
                climberRef = new ClimberRef(climber.getLogin());
                climberRef.setFullName(climber.getFullName());
                climberRef.setCountryBigram(climber.getCountryBigram());
            }
            try {
                //moche : on remplace le climber par une ref pour éviter des références cycliques lors de la serialisation.
                value.setClimber(climberRef);
                delegate.write(out, value);
            } finally {
                //remet la valeur d'origine...
                value.setClimber(climber);
            }
        }
    }

    /**
     * Renvoie le couple login/mot de passe. Null s'il est non défini ou partiellement.
     * @param context
     * @return
     */
    public static String[] getCredentials(Context context) {
        SharedPreferences globalSettings = globalSettings(context).get();
        String login = globalSettings.getString(ProjectsActivity.LOGIN_KEY,null);
        String encryptedPassword = globalSettings.getString(ProjectsActivity.PASSWORD_KEY,null);
        if(login != null && encryptedPassword != null) {
            String something = EncryptionHelper.doSomething(login);
            String password = EncryptionHelper.decrypt(encryptedPassword,something);
            return new String[]{login,password};
        }
        return null;
    }

    public static void invalidateCredentials(Context context) {
        SharedPreferences globalSettings = globalSettings(context).get();
        globalSettings.edit().remove(ProjectsActivity.LOGIN_KEY).commit();
        globalSettings.edit().remove(ProjectsActivity.PASSWORD_KEY).commit();
    }



}
