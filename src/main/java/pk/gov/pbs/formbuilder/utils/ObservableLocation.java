package pk.gov.pbs.formbuilder.utils;

import android.location.Location;

public class ObservableLocation extends Location {
    OnLocationChangeListener mChangeListener;
    public ObservableLocation(String provider) {
        super(provider);
    }

    public ObservableLocation(Location l) {
        super(l);
    }

    @Override
    public void setProvider(String provider) {
        super.setProvider(provider);
        if (mChangeListener != null)
            mChangeListener.onLocationChange(this);
    }

    @Override
    public void set(Location l) {
        super.set(l);
        if (mChangeListener != null)
            mChangeListener.onLocationChange(this);
    }

    @Override
    public void reset() {
        super.reset();
        if (mChangeListener != null)
            mChangeListener.onLocationChange(this);
    }

    public void setOnLocationChangeListener(OnLocationChangeListener handler){
        mChangeListener = handler;
    }

    public interface OnLocationChangeListener {
        void onLocationChange(Location newLocation);
    }
}
