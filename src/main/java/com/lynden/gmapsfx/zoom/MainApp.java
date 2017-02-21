package com.lynden.gmapsfx.zoom;


import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.DirectionStatus;
import com.lynden.gmapsfx.service.directions.DirectionsResult;
import com.lynden.gmapsfx.service.directions.DirectionsServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationResult;
import com.lynden.gmapsfx.service.elevation.ElevationServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationStatus;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;
import com.lynden.gmapsfx.service.geocoding.GeocodingServiceCallback;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.util.*;

public class MainApp extends Application implements MapComponentInitializedListener,
        ElevationServiceCallback, GeocodingServiceCallback, DirectionsServiceCallback {


    private GoogleMapView mapComponent;
    protected GoogleMap map;
    protected DirectionsPane directions;

    private ComboBox<MapTypeIdEnum> mapTypeCombo;

    private Map<LatLong,Marker> markers = new HashMap();
    private Number zoomCoef = 9;
    private InfoWindow window ;
    private LatLong ll;
    private String address = "";


    private Number getZoomCoef() {
        return zoomCoef;
    }

    private void setZoomCoef(Number zoomCoef) {
        this.zoomCoef = zoomCoef;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void elevationsReceived(ElevationResult[] results, ElevationStatus status) {

    }

    @Override
    public void mapInitialized() {


        LatLong center = new LatLong(54.187211, 45.183642);
        mapComponent.addMapReadyListener(() -> {

        });

        MapOptions options = new MapOptions();
        options.center(center)
                .zoom(getZoomCoef().intValue())
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.TERRAIN);

        map = mapComponent.createMap(options,false);
        directions = mapComponent.getDirec();

        map.setHeading(123.2);

        map.centerProperty().addListener((ObservableValue<? extends LatLong> obs, LatLong o, LatLong n) -> {
            //lblCenter.setText(n.toString());
        });

        //lblZoom.setText(Integer.toString(map.getZoom()));
        map.zoomProperty().addListener((ObservableValue<? extends Number> obs, Number o, Number n) -> {
            setZoomCoef(n);
            setContent();
        });

        map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
            ll = new LatLong((JSObject) obj.getMember("latLng"));


            GeocodingService gs = new GeocodingService();
            GeocodingServiceCallback calBack = null;
            gs.reverseGeocode(ll.getLatitude(), ll.getLongitude(), this);

            MarkerOptions markerOptions = new MarkerOptions();
            LatLong markerLatLong = new LatLong(ll.getLatitude(), ll.getLongitude());

            markerOptions.position(markerLatLong)
                    .title("My new Marker")
                    .visible(true);

            for (Marker m : markers.values()){
                map.removeMarker(m);
            }


            Marker myMarker = markers.get(ll);

            if (null==myMarker){
                myMarker = new Marker(markerOptions);
                markers.put(ll,myMarker);
            }



            myMarker.setVisible(true);

            //markers.clear();
            map.addMarker(myMarker);




            //Platform.runLater(
              //      () -> {

                         InfoWindowOptions infoOptions = new InfoWindowOptions();
                                infoOptions.position(center);


                        window= new InfoWindow(infoOptions);
                        window.open(map, myMarker);


                //    }
            //);





        });

    }


    @Override
    public void geocodedResultsReceived(GeocodingResult[] results, GeocoderStatus status) {
        if(status.equals(GeocoderStatus.OK)){
            for(GeocodingResult e : results){
                //System.out.println(e.getVariableName());

                //System.out.println("GEOCODE: " + e.getFormattedAddress() + "\n" /*+ e.toString()*/);
                address = e.getFormattedAddress();
                setContent();
                break;
            }

        }

    }


    private void setContent() {
        window.setContent("<h2>"+getAddress()+"</h2><h3>" + ll.toString() + "</h3><h3>Zoom: " + getZoomCoef() + "</h3> ");
    }


    @Override
    public void directionsReceived(DirectionsResult results, DirectionStatus status) {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Java version: " + System.getProperty("java.home"));
        mapComponent = new GoogleMapView(Locale.getDefault().getLanguage(), null);
        mapComponent.addMapInializedListener(this);

        BorderPane bp = new BorderPane();


        mapTypeCombo = new ComboBox<>();
        mapTypeCombo.setOnAction( e -> map.setMapType(mapTypeCombo.getSelectionModel().getSelectedItem() ));
        mapTypeCombo.setDisable(true);

        Button btnType = new Button("Map type");
        btnType.setOnAction(e -> map.setMapType(MapTypeIdEnum.HYBRID));

        bp.setCenter(mapComponent);

        Scene scene = new Scene(bp);
        primaryStage.setScene(scene);
        primaryStage.show();
    }



}
