package com.averroes.hanouti.include;

public interface LocationPermissionMethods {

    static final int LOCATION_REQUEST = 100;

    abstract boolean checkLocationPermission();

    abstract void requestLocationPermission();

    abstract void detectLocation();

    abstract void findAddress();
}
