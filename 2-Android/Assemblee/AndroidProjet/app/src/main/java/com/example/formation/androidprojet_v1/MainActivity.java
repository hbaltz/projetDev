package com.example.formation.androidprojet_v1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GraphicsLayer.RenderingMode;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Feature;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.table.TableException;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class MainActivity extends Activity  {

    /**
     * Déclaration des variables globales :
     */

    // Define ArcGIS Elements
    private MapView mMapView;

    private final String extern = Environment.getExternalStorageDirectory().getPath();

    // TODO : chemin qui change en fonction SD card ou non : trouver automatiquement

    // Sd card :
    private final String chTpk = "/ProjArcades/ArcGIS/";
     /*
    // Sans sd card :
    private final String chTpk = "/Android/data/com.example.formation.androidprojet_v1/ArcGIS/";
    */

    // Variable pour image de fond :
    private String tpkPath = chTpk +"arcades.tpk";
    private String tpkPath0 = chTpk +"niveau_0.tpk";
    private String tpkPath1 = chTpk +"niveau_1.tpk";
    private String tpkPath2 = chTpk +"niveau_2.tpk";

    private TiledLayer mTileLayer = new ArcGISLocalTiledLayer(extern + tpkPath);
    private TiledLayer mTileLayer0 = new ArcGISLocalTiledLayer(extern + tpkPath0);
    private TiledLayer mTileLayer1 = new ArcGISLocalTiledLayer(extern + tpkPath1);
    private TiledLayer mTileLayer2 = new ArcGISLocalTiledLayer(extern + tpkPath2);

    private GraphicsLayer mGraphicsLayer = new GraphicsLayer(RenderingMode.DYNAMIC);

    private RouteTask mRouteTask = null;
    private NAFeaturesAsFeature mStops = new NAFeaturesAsFeature();

    private Locator mLocator = null;
    private Spinner dSpinner;

    // Variables utiles pour la gestion du multi-étage :
    Spinner spinnerEtgSel;
    private boolean etgsSelected = false;
    private boolean etg0Selected = false;
    private boolean etg1Selected = false;
    private boolean etg2Selected = false;

    // Variables utiles à la gestion du QR_code :
    private Geometry geom_QR_code = null;

    private Geometry projection_niv0 = null;
    private Geometry projection_niv1 = null;
    private Geometry projection_niv2 = null;
    // Geometrie union :
    private Geometry geometries_niveau0 = null;
    private Geometry geometries_niveau1 = null;
    private Geometry geometries_niveau2 = null;
    // Géomtrie intersections :
    private Geometry geom = null;
    private Geometry geom_intersect_niv0 = null;
    private Geometry geom_intersect_niv1 = null;
    private Geometry geom_intersect_niv2 = null;

    // Définiton géométrie engine :
    private GeometryEngine geomen = new GeometryEngine();

    // Référence spatiale :
    private SpatialReference WKID_RGF93 = SpatialReference.create(102110);

    // Variables utiles pour la récupération des magasins :
    // Features :
    private Feature[] mag_niv0 = new Feature[12];
    private Feature[] mag_niv1 = new Feature[66];
    private Feature[] mag_niv2 = new Feature[64];
    // Geometries :
    private Geometry[] mag_niv0_geom = new Geometry[12];
    private Geometry[] mag_niv1_geom = new Geometry[66];
    private Geometry[] mag_niv2_geom = new Geometry[64];
    // Geometries projetees :
    private Geometry projection_mag_niv0 = null;
    private Geometry projection_mag_niv1 = null;
    private Geometry projection_mag_niv2 = null;
    // Geometries d'un magasin :
    private Geometry mag_niveau0 = null;
    private Geometry mag_niveau1 = null;
    private Geometry mag_niveau2 = null;
    // Liste nom  :
    private List lst_mag_niveau0 = new Vector();
    private List lst_mag_niveau1 = new Vector();
    private List lst_mag_niveau2 = new Vector();
    // Liste type  :
    private List lst_types_niveau0 = new Vector();
    private List lst_types_niveau1 = new Vector();
    private List lst_types_niveau2 = new Vector();
    // Test
    private Geometry pt_fnac = null;

    // Variables utiles à la récupérations des arcs :
    // Géometries :
    private Geometry[] array_geom_niv0 = new Geometry[127];
    private Geometry[] array_geom_niv1_1 = new Geometry[380];
    private Geometry[] array_geom_niv1_2 = new Geometry[377];
    private Geometry[] array_geom_niv2_1 = new Geometry[380];
    private Geometry[] array_geom_niv2_2 = new Geometry[400];
    private Geometry[] array_geom_niv1 = new Geometry[2];
    private Geometry[] array_geom_niv2 = new Geometry[2];

    // Gestion itinéraire :
    private int routeHandle = -1;

    // Variable de restrictions :
    private CheckBox checkBoxRes = null;
    private boolean estRestreint = false;

    //Saisie auto :
    private List lst_nom_mag = new ArrayList();
    private AutoCompleteTextView textViewArr;
    private AutoCompleteTextView textViewDep;

    //Ppv :
    private int niveau_dep = 0;
    private int niveau_arr = 0;

    // Définition des points de départ et d'arrivé :
    private Geometry depart;
    private Geometry arrive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Spinner element pour changement etage
        spinnerEtgSel = (Spinner) findViewById(R.id.spinnerEtgSelc);
        // Spinner click listener pour changement etage
        spinnerEtgSel.setOnItemSelectedListener(new BoutonEtageListener());

        File tpk = new File(extern + tpkPath);
        Log.d("RoutingAndGeocoding", "Find tpk: " + tpk.exists());
        Log.d("RoutingAndGeocoding", "Initialized tpk: " + mTileLayer.isInitialized());

        // Find the directions spinner
        dSpinner = (Spinner) findViewById(R.id.directionsSpinner);
        dSpinner.setEnabled(false);

        // Retrieve the map and initial extent from XML layout
        mMapView = (MapView) findViewById(R.id.map);

        // Mise en place des fonds et visibilité = false :
        mMapView.addLayer(mTileLayer0);
        mTileLayer0.setVisible(false);

        mMapView.addLayer(mTileLayer1);
        mTileLayer1.setVisible(false);

        mMapView.addLayer(mTileLayer2);
        mTileLayer2.setVisible(false);

        mMapView.addLayer(mTileLayer);
        mTileLayer.setVisible(false);

        // Ajout couche graphique :
        mMapView.addLayer(mGraphicsLayer);

        //Restriction :
        checkBoxRes = (CheckBox)findViewById(R.id.checkBoxRes);
        String resTxt = getResources().getString(R.string.rest);
        checkBoxRes.setText(resTxt);
        checkBoxRes.setOnClickListener(new checkedListener());

        // Récupération des élémenst dans la bdd :
        accesBdd();

        // QR code
        Button qrButton = (Button) findViewById(R.id.scan_button);
        String qrTxt = getResources().getString(R.string.qr);
        qrButton.setText(qrTxt);
        qrButton.setOnClickListener(new BoutonQRcodeListener());

        // Saisie automatique
        // Liste magaisn :
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, lst_nom_mag);
        // Bouton :
        textViewDep = (AutoCompleteTextView)
                findViewById(R.id.dep_magasin);
        String depTxt = getResources().getString(R.string.dep);
        textViewDep .setHint(depTxt);
        textViewDep .setAdapter(adapter);
        textViewDep .setThreshold(1); // on commence la recherche automatique dès la première lettre ecrite
        textViewDep .setOnItemClickListener(new BoutonSaisieAutomatiqueDepListener());

        textViewArr = (AutoCompleteTextView)
                findViewById(R.id.arr_magasin);
        String arrTxt = getResources().getString(R.string.arr);
        textViewArr .setHint(arrTxt);
        textViewArr .setAdapter(adapter);
        textViewArr .setThreshold(1); // on commence la recherche automatique dès la première lettre ecrite
        textViewArr .setOnItemClickListener(new BoutonSaisieAutomatiqueArrListener());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Gestion des QR codes :
     */

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Nous utilisons la classe IntentIntegrator et sa fonction parseActivityResult pour parser le résultat du scan
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            // Récupération référence spatiale de  la vue :
            SpatialReference mapRef = mMapView.getSpatialReference();

            // Nous récupérons le contenu du code barre
            String scanContent = scanningResult.getContents();

            // Nous récupérons le format du code barre
            String scanFormat = scanningResult.getFormatName();

            TextView scan_format = (TextView) findViewById(R.id.scan_format);
            TextView scan_content = (TextView) findViewById(R.id.scan_content);

            // Nous affichons le résultat dans nos TextView
            scan_format.setText("FORMAT: " + scanFormat);
            scan_content.setText("CONTENT: " + scanContent);
            Log.d("Scan content", scanContent);

            // Test sur le different QR code scanné
            // On utilise ces tests pour définir le point de départ ou des points intermédiaires par exemple
            if(scanContent.equals( "QR code 01" ) )
            {
                Log.d("QR_code","QR code 01");
                // On marque la geometrie du QR code sur la carte
                // Rappel,on test avec le magasin "La grande recre"
                Geometry projection = geomen.project(geom_QR_code, WKID_RGF93, mapRef);
                mGraphicsLayer.addGraphic(new Graphic(projection, new SimpleMarkerSymbol(Color.RED, 10, STYLE.CROSS)));
                //mMapView.getCallout().hide();
            }
            if(scanContent.equals( "QR code 02" ) ) {Log.d("QR_code","QR code 02");}
            if(scanContent.equals( "QR code 03" ) ) {Log.d("QR_code","QR code 03");}
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Aucune donnée reçu!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// LISTENERS : ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Listener du bouton de la restriction.
     * */
    class checkedListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SpatialReference mapRef = mMapView.getSpatialReference();

            if (((CheckBox) v).isChecked()) {
                estRestreint = true;
                clearAffich();
                calculerIti(mapRef);
            } else {
                estRestreint = false;
                clearAffich();
                calculerIti(mapRef);
            }
        }
    }


    /**
     * Listener du bouton de choissi d'étage :
     */
    class BoutonEtageListener implements OnItemSelectedListener {

        /**
         * Définition des évenements :
         */

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // On selecting a spinner item
            String etageSelec = parent.getItemAtPosition(position).toString();

            // Showing selected spinner item
            Toast.makeText(parent.getContext(), "Selected: " + etageSelec, Toast.LENGTH_LONG).show();

            // On recupere les noms des etages qui sont stockés dans ressources.strings.values
            String[] nom_etage = getResources().getStringArray(R.array.etage_array);

            // Test suivant la selection de l'utilisateur:
            if (etageSelec.equals(nom_etage[0])) {
                etgsSelected = false;
                etg0Selected = true;
                etg1Selected = false;
                etg2Selected = false;
            }
            if (etageSelec.equals(nom_etage[1])) {
                etgsSelected = false;
                etg0Selected = false;
                etg1Selected = true;
                etg2Selected = false;
            }
            if (etageSelec.equals(nom_etage[2])) {
                etgsSelected = false;
                etg0Selected = false;
                etg1Selected = false;
                etg2Selected = true;
            }
            if (etageSelec.equals(nom_etage[3])) {
                etgsSelected = true;
                etg0Selected = false;
                etg1Selected = false;
                etg2Selected = false;
            }

            mTileLayer.setVisible(etgsSelected);
            mTileLayer0.setVisible(etg0Selected);
            mTileLayer1.setVisible(etg1Selected);
            mTileLayer2.setVisible(etg2Selected);

            ////////////////////////////////////////////////////////////////////////////////////////

            // Gestion affichage au moment du changement d'étage :
            afficherIti();
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    class BoutonQRcodeListener implements View.OnClickListener {

        //QR code
        @Override
        public void onClick(View v) {
            //QR code
            if (v.getId() == R.id.scan_button) {
                // on lance le scanner au clic sur notre bouton
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
                //new IntentIntegrator(this).initiateScan();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Listener bouton saise auto
     * Il y en a deux car aucun moyen e récupérere facilement l'id de AutoCompleteTextView sur laquelle on clique
     */

    // Départ :
    class BoutonSaisieAutomatiqueDepListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
            Log.d("View_id",""+view.getId());
            // Initialisation :
            // Référence spatiale :
            SpatialReference mapRef = mMapView.getSpatialReference();

            // Bolléen (vrai si un point est selectionné, faux sinon) :
            boolean trouve = false;

            // Définition du symbole des points :
            Drawable marqueur = getResources().getDrawable(R.drawable.ic_action_marqueur);
            Symbol symStop = new PictureMarkerSymbol(marqueur);

            // Définition de la géométrie :
            Geometry ptTest = null;

            // Nombre de point sélectionnés :
            int tStop = mStops.getFeatures().size();

            // Remise à zero des stops :
            // Si il y a plus de deus stops au départ
            // On supprime réinistiallise la vue et on remet en fonction du bouton sélectionné le départ
            // ou l'arrivé (on remet le départ si on modifie l'arrivé et inversement)
            if( tStop >=2 ) {
                mStops.clearFeatures();
                clearAffich();
                ajouterPoint(depart, symStop);
            }

            // On selectionne le magasin dans la liste de saisie automatique
            String item = parent.getItemAtPosition(position).toString();
            Log.v("mag_selectionne",item);

            ptTest = trouverPtSel(item, true);
            if (ptTest !=null){
                trouve = true;
            }

            // Lorsque qu'on a trouvé un point
            // On gère le fait que ce soit le départ ou l'arrivé
            // Dans tout les cas on l'ajoute au stop et on l'affiche
            if(trouve){
                depart = geomen.project(ptTest, WKID_RGF93, mapRef);
                ajouterPoint(depart, symStop);
            }

            // On récupére à nouveau le nombre de stops
            tStop = mStops.getFeatures().size();

            Log.d("nStop","" + tStop );

            // Si on a 2 stops on calcule et on affiche l'itinéraire
            if( tStop >= 2) {
                calculerIti(mapRef);
                afficherPpv(mapRef);
            }
        }
    }

    // Arrivée :
    class BoutonSaisieAutomatiqueArrListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
            // Initialisation :
            // Référence spatiale :
            SpatialReference mapRef = mMapView.getSpatialReference();

            // Bolléen (vrai si un point est selectionné, faux sinon) :
            boolean trouve = false;

            // Définition du symbole des points :
            Drawable marqueur = getResources().getDrawable(R.drawable.ic_action_marqueur);
            Symbol symStop = new PictureMarkerSymbol(marqueur);

            // Définition de la géométrie :
            Geometry ptTest = null;

            // Nombre de point sélectionnés :
            int tStop = mStops.getFeatures().size();

            // Remise à zero des stops :
            // Si il y a plus de deus stops au départ
            // On supprime réinistiallise la vue et on remet en fonction du bouton sélectionné le départ
            // ou l'arrivé (on remet le départ si on modifie l'arrivé et inversement)
            if( tStop >=2 ) {
                mStops.clearFeatures();
                clearAffich();
                ajouterPoint(depart, symStop);
            }

            // On selectionne le magasin dans la liste de saisie automatique
            String item = parent.getItemAtPosition(position).toString();
            Log.v("mag_selectionne",item);

            ptTest = trouverPtSel(item, false);
            if (ptTest !=null){
                trouve = true;
            }

            // Lorsque qu'on a trouvé un point
            // On gère le fait que ce soit le départ ou l'arrivé
            // Dans tout les cas on l'ajoute au stop et on l'affiche
            if(trouve){
                Log.d("if_arr", "OK");
                arrive = geomen.project(ptTest, WKID_RGF93, mapRef);
                ajouterPoint(arrive, symStop);
            }

            // On récupére à nouveau le nombre de stops
            tStop = mStops.getFeatures().size();

            Log.d("nStop","" + tStop );

            // Si on a 2 stops on calcule et on affiche l'itinéraire
            if( tStop >= 2) {
                calculerIti(mapRef);
                afficherPpv(mapRef);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// FONCTIONS : ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void accesBdd(){
        // TODO : Modification automatique en fonction du type d'appareil (SD ou non)

        // Get the external directory

        // SdCard
        String locatorPath = chTpk + "/Geocoding/MGRS.loc";
        String networkPath = chTpk + "/Routing/base_de_donnees.geodatabase";


        String networkName = "GRAPH_Final_ND";

        // Attempt to load the local geocoding and routing data
        try {
            mLocator = Locator.createLocalLocator(extern + locatorPath);
            mRouteTask = RouteTask.createLocalRouteTask(extern + networkPath, networkName);

            ////////////////////////////////////////////////////////////////////////////////

            // open a local geodatabase
            Geodatabase gdb = new Geodatabase(extern + networkPath);

            // On prend un point connu pour tester QR code, en utilisant un magasin existant
            // Ensuite on integrera directement les QR code dans la geodatabase
            // Il sera judicieux de l'integrer par Android pour éviter à l'utilisateur
            // programmeur de recréer la geodatabase

            // Magasin test : La grande recre
            geom_QR_code = gdb.getGeodatabaseTables().get(0).getFeature(1).getGeometry();

            ////////////////////////////////////////////////////////////////////////////////
            // Récupération de la référence spatiale :
            SpatialReference mapRef = mMapView.getSpatialReference();

            // TODO : un arc par niveau ? Gain temps et efficacité ?

            GeodatabaseFeatureTable tab_niv0 = gdb.getGeodatabaseTables().get(11);
            GeodatabaseFeatureTable tab_niv1 = gdb.getGeodatabaseTables().get(12);
            GeodatabaseFeatureTable tab_niv2 = gdb.getGeodatabaseTables().get(13);

            Log.d("tab1", tab_niv1.getTableName());

            // Récupération des arcs dans la géodatabase  :
            // Comme nous avons plus de 512 arcs sur les étages 1 et 2
            // Nous avons dû procéder en deux fois pour récupérer les arcs de ces étages

            // Définition du nombre d'arcs :
            int i = array_geom_niv0.length;
            int l1 = array_geom_niv1_1.length;
            int l2 = array_geom_niv1_2.length;
            int m1 = array_geom_niv2_1.length;
            int m2 = array_geom_niv2_2.length;

            Geometry poubelle = new Polyline(); // Varaible utile si pas d'objet dans la base

            // Etage 0 :
            for(int j=1; j<=i; j++){
                if (tab_niv0.checkFeatureExists(j)) {
                    array_geom_niv0[j-1] = tab_niv0.getFeature(j,WKID_RGF93).getGeometry();
                } else {array_geom_niv0[j-1] = poubelle;}
            }

            // Etage 1_1 :
            for(int j=1; j<=l1; j++){
                if (tab_niv1.checkFeatureExists(j)) {
                    array_geom_niv1_1[j-1] = tab_niv1.getFeature(j,WKID_RGF93).getGeometry();
                } else {array_geom_niv1_1[j-1] = poubelle;}
            }

            // Etage 1_2 :
            int k1 = 0;
            double longTot = 0;
            for(int j=l1+1; j<=l1+l2; j++){
                if (tab_niv1.checkFeatureExists(j)) {
                    array_geom_niv1_2[k1] = tab_niv1.getFeature(j,WKID_RGF93).getGeometry();
                } else {array_geom_niv1_2[k1] = poubelle;}
                k1 = k1+1;
            }

            // Etage 2_1 :
            for(int j=1; j<=m1; j++){
                if (tab_niv2.checkFeatureExists(j)) {
                    array_geom_niv2_1[j-1] = tab_niv2.getFeature(j,WKID_RGF93).getGeometry();
                } else {array_geom_niv2_1[j-1] = poubelle;}
            }

            // Etage 2_2 :
            int k2 = 0;
            for(int j=m1+1; j<=m1+m2; j++){
                if (tab_niv2.checkFeatureExists(j)) {
                    array_geom_niv2_2[k2] = tab_niv2.getFeature(j,WKID_RGF93).getGeometry();
                } else {array_geom_niv2_2[k2] = poubelle;}
                k2 = k2+1;
            }

            ////////////////////////////////////////////////////////////////////////////////

            // Union des arcs :

            // Niveau 0:
            geometries_niveau0 = geomen.union(array_geom_niv0, WKID_RGF93);

            // Niveau 1 :
            array_geom_niv1[0] = geomen.union(array_geom_niv1_1, WKID_RGF93);
            array_geom_niv1[1] = geomen.union(array_geom_niv1_2, WKID_RGF93);
            geometries_niveau1 = geomen.union(array_geom_niv1, WKID_RGF93);

            // Niveau 2 :
            array_geom_niv2[0] = geomen.union(array_geom_niv2_1, WKID_RGF93);
            array_geom_niv2[1] = geomen.union(array_geom_niv2_2, WKID_RGF93);
            geometries_niveau2 = geomen.union(array_geom_niv2, WKID_RGF93);

            // logs :
            Log.d("geometries_niveau0", "" + geometries_niveau0.calculateLength2D());
            Log.d("geometries_niveau1", "" + geometries_niveau1.calculateLength2D());
            Log.d("geometries_niveau2", "" + geometries_niveau2.calculateLength2D());

            ////////////////////////////////////////////////////////////////////////////////////////

            // Récupération des magasins :
            for(int v=0; v<=2; v++){
                GeodatabaseFeatureTable mag = gdb.getGeodatabaseTables().get(v);

                long nbr_lignes = mag.getNumberOfFeatures();
                for(int l=1; l<=nbr_lignes; l++){
                    if (v==0) {
                        if (mag.checkFeatureExists(l)) {
                            mag_niv0[l-1] = mag.getFeature(l);
                        } else {mag_niv0[l-1] = null;}
                    } else if (v==1) {
                        if (mag.checkFeatureExists(l)) {
                            mag_niv1[l-1] = mag.getFeature(l);
                        } else {mag_niv1[l-1] = null;}
                    } else if (v==2) {
                        if (mag.checkFeatureExists(l)) {
                            mag_niv2[l-1] = mag.getFeature(l);
                        } else {mag_niv2[l-1] = null;}
                    }
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////

            // Récupération des géométries, noms type :
            // Etage 0
            int len0 = mag_niv0.length;
            for(int k=0; k<len0; k++) {

                Feature Mag =  mag_niv0[k];

                // Récupération géométrie :
                mag_niv0_geom[k] = mag_niv0[k].getGeometry();

                // Récupération nom et type :
                Map<String, Object> lignes = Mag.getAttributes();
                Object type = lignes.get("TYPE");
                Object nom_mag = lignes.get("NOM");
                lst_types_niveau0.add(type);
                lst_mag_niveau0.add(nom_mag);
                lst_nom_mag.add(nom_mag);
            }

            // Etage 1
            int len1 = mag_niv1.length;
            for(int k=0; k<len1; k++) {

                Feature Mag =  mag_niv1[k];

                // Récupération géométrie :
                mag_niv1_geom[k] = mag_niv1[k].getGeometry();

                // Récupération nom et type :
                Map<String, Object> lignes = Mag.getAttributes();
                Object type = lignes.get("TYPE");
                Object nom_mag = lignes.get("NOM");
                lst_types_niveau1.add(type);
                lst_mag_niveau1.add(nom_mag);
                lst_nom_mag.add(nom_mag);
            }

            // Etage 2
            int len2 = mag_niv0.length;
            for(int k=0; k<len2; k++) {

                Feature Mag =  mag_niv2[k];

                // Récupération géométrie :
                mag_niv2_geom[k] = mag_niv2[k].getGeometry();

                // Récupération nom et type :
                Map<String, Object> lignes = Mag.getAttributes();
                Object type = lignes.get("TYPE");
                Object nom_mag = lignes.get("NOM");
                lst_types_niveau2.add(type);
                lst_mag_niveau2.add(nom_mag);
                lst_nom_mag.add(nom_mag);
            }

            ////////////////////////////////////////////////////////////////////////////////////////

            // Union des magasins :

            mag_niveau0 = geomen.union(mag_niv0_geom, WKID_RGF93);
            mag_niveau1 = geomen.union(mag_niv1_geom, WKID_RGF93);
            mag_niveau2 = geomen.union(mag_niv2_geom, WKID_RGF93);

            ////////////////////////////////////////////////////////////////////////////////////////


            // Test :

            depart = gdb.getGeodatabaseTables().get(1).getFeature(35).getGeometry();
            depart = geomen.project(depart, WKID_RGF93, mapRef);


        } catch (Exception e) {
            popToast("Error while initializing :" + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fonction qui retrouve un magasin item dans la liste de magasins
     * @param item
     * @param estDepart
     * @return
     */
    public Geometry trouverPtSel(String item, boolean estDepart){

        // Bolléen (vrai si un point est selectionné, faux sinon) :
        boolean trouve = false;

        // Définition de la géométrie :
        Geometry ptTest = null;

        // On parcourt la liste récupérer au début dans la geodatabase et on récupère la geometrie
        // correspondant au magasin choisie par l'utilisateur
        int len0 = mag_niv0.length;
        for(int k=0; k<len0; k++) {
            Feature Mag =  mag_niv0[k];
            Map<String, Object> lignes = Mag.getAttributes();
            Object nom_mag = lignes.get("NOM");
            if(nom_mag.equals(item)){
                ptTest = mag_niv0[k].getGeometry();
                trouve = true;
                if(estDepart) {niveau_dep = 0;}
                else{niveau_arr = 0;}
            }
        }
        if(!trouve){
            int len1 = mag_niv1.length;
            for(int k=0; k<len1; k++) {
                Feature Mag =  mag_niv1[k];
                Map<String, Object> lignes = Mag.getAttributes();
                Object nom_mag = lignes.get("NOM");
                if(nom_mag.equals(item)){
                    ptTest = mag_niv1[k].getGeometry();
                    trouve = true;
                    if(estDepart) {niveau_dep = 1;}
                    else{niveau_arr = 1;}
                }
            }
        }
        if(!trouve){
            int len2 = mag_niv2.length;
            for(int k=0; k<len2; k++) {
                Feature Mag =  mag_niv2[k];
                Map<String, Object> lignes = Mag.getAttributes();
                Object nom_mag = lignes.get("NOM");
                if(nom_mag.equals(item)){
                    ptTest = mag_niv2[k].getGeometry();
                    if(estDepart) {niveau_dep = 2;}
                    else{niveau_arr = 2;}
                }
            }
        }
        return ptTest;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fonction qui ajoute une géométrie point au stops et sur le graphe avec le symbole symbol
     * @param point
     * @param symbol
     */
    public void ajouterPoint(Geometry point, Symbol symbol){
        mGraphicsLayer.addGraphic(new Graphic(point, symbol));
        StopGraphic stop = new StopGraphic(point);
        mStops.addFeature(stop);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fonction qui réinisitallise l'affichage :
     */
    public void clearAffich(){
        mGraphicsLayer.removeAll();
        mMapView.getCallout().hide();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fonction qui calcule l'tinéraire
     */
    public void calculerIti(SpatialReference mapRef){

        // Return default behavior if we did not initialize properly.
        if (mRouteTask == null) {
            popToast("RouteTask uninitialized.", true);
        }

        try {
            // Set the correct input spatial reference on the stops and the
            // desired output spatial reference on the RouteParameters object.
            RouteParameters params = mRouteTask.retrieveDefaultRouteTaskParameters();
            params.setOutSpatialReference(mapRef);
            mStops.setSpatialReference(mapRef);

            if(estRestreint){
                String[] restrictions = {"Restriction"};
                params.setRestrictionAttributeNames(restrictions);
            } else{
                String[] restrictions = {""};
                params.setRestrictionAttributeNames(restrictions);
            }

            // Set the stops and since we want driving directions,
            // returnDirections==true
            params.setStops(mStops);
            params.setReturnDirections(true);

            // Perform the solve
            RouteResult results = mRouteTask.solve(params);

            // Grab the results; for offline routing, there will only be one
            // result returned on the output.
            Route result = results.getRoutes().get(0);

            ////////////////////////////////////////////////////////////////////////////////////////

            // On projete les arcs dans le repère local :

            projection_niv0 = geomen.project(geometries_niveau0, WKID_RGF93, mapRef);
            projection_niv1 = geomen.project(geometries_niveau1, WKID_RGF93, mapRef);
            projection_niv2 = geomen.project(geometries_niveau2, WKID_RGF93, mapRef);

            ////////////////////////////////////////////////////////////////////////////////////////

            geom = result.getRouteGraphic().getGeometry();

            // On intersecte l'itinéraire avec les arcs :
            geom_intersect_niv0 = geomen.intersect(geom, projection_niv0, mapRef);
            geom_intersect_niv1 = geomen.intersect(geom, projection_niv1, mapRef);
            geom_intersect_niv2 = geomen.intersect(geom, projection_niv2, mapRef);

            ////////////////////////////////////////////////////////////////////////////////////

            //Gestion affichage au moment du calcul d'itinéraire :
            spinnerEtgSel.setSelection(niveau_dep);
            afficherIti();

            ////////////////////////////////////////////////////////////////////////////////////
            mMapView.getCallout().hide();

        } catch (Exception e) {
            popToast("Solve Failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fonction pour afficher l'itinéraire en fonction de l'étage sélectionné
     */
    public void afficherIti(){
        // Défintion symbole pour l'itinéraire :
        SimpleLineSymbol ligSym = new SimpleLineSymbol(0x99990055, 5, SimpleLineSymbol.STYLE.fromString("DASH"));

        // Remove any previous route Graphics
        mGraphicsLayer.removeGraphic(routeHandle);

        // On ne visualise que l'itinéraire au niveau selectionné :
        if(geom_intersect_niv0 != null && etg0Selected) {
            if (!geom_intersect_niv0.isEmpty()) {
                routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom_intersect_niv0, ligSym));
                Log.d("geom0_inter_length", ": " + geom_intersect_niv0.calculateLength2D());
            }
        }

        if(geom_intersect_niv1 != null && etg1Selected) {
            if (!geom_intersect_niv1.isEmpty()) {
                routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom_intersect_niv1, ligSym));
                Log.d("geom1_inter_length", ": " + geom_intersect_niv1.calculateLength2D());
            }
        }

        if(geom_intersect_niv2 != null  && etg2Selected) {
            if (!geom_intersect_niv2.isEmpty()) {
                routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom_intersect_niv2, ligSym));
                Log.d("geom2_inter_length", ": " + geom_intersect_niv2.calculateLength2D());
            }
        }

        if(geom!= null && etgsSelected) {
            if (!geom.isEmpty()) {
                routeHandle = mGraphicsLayer.addGraphic(new Graphic(geom, ligSym));
                Log.d("geom_inter_length", ": " + geom.calculateLength2D());
            }
        }
    }


    /**
     * Fonction qui affiche le magasin le plus proche du point de départ :
     * @param mapRef
     */
    public void afficherPpv(SpatialReference mapRef){
        Log.d("nivAll","Dedans");

        // On projete les magasins en mapRef :
        projection_mag_niv0 = geomen.project(mag_niveau0, WKID_RGF93, mapRef);
        projection_mag_niv1 = geomen.project(mag_niveau1, WKID_RGF93, mapRef);
        projection_mag_niv2 = geomen.project(mag_niveau2, WKID_RGF93, mapRef);

        //depart = geomen.project(depart, WKID_RGF93, mapRef);

        // Différence entre le point et les autres magasins
        Geometry diff_niv0 = geomen.difference(projection_mag_niv0, depart, mapRef);
        Geometry diff_niv1 = geomen.difference(projection_mag_niv1, depart, mapRef);
        Geometry diff_niv2 = geomen.difference(projection_mag_niv2, depart, mapRef);

        Log.d("Diff", "0 : " + diff_niv0 + " 1 : " + diff_niv1 + " 2 : " + diff_niv2);

        // Distance géométrique
        double distance_niv0 = geomen.distance(depart, diff_niv0, mapRef);
        double distance_niv1 = geomen.distance(depart, diff_niv1, mapRef);
        double distance_niv2 = geomen.distance(depart, diff_niv2, mapRef);

        Log.d("Dist", "0 : " + distance_niv0 + " 1 : " + distance_niv1 + " 2 : " + distance_niv2);


        // Définition de l'unité :
        Unit meter = Unit.create(LinearUnit.Code.METER);

        // Initialisation des variables utile au calcul du ppv :
        String texte = null;
        Geometry mag = null;
        int taille = 14;
        double dist_ref = 1000;
        int color = Color.rgb(255, 1, 1);

        if (niveau_dep == 0){
            Log.d("niv0","Dedans");

            Polygon buff_niv0 = geomen.buffer(depart, mapRef, distance_niv0, meter);
            Geometry magasin = geomen.intersect(buff_niv0, projection_mag_niv0, mapRef);

            // On cherhce le magasin le plus proche
            // c'est-à-dire à la distance minimale du point de départ
            for (int r=0; r<lst_mag_niveau0.size(); r++){
                Geometry mag_niv0_r = geomen.project(mag_niv0_geom[r], WKID_RGF93, mapRef);
                double dist_mag0 = geomen.distance(mag_niv0_r,magasin, mapRef);

                Log.d("magasin",""+magasin);
                Log.d("mag_niv0_r",""+mag_niv0_r);
                Log.d("dist_mag0",""+dist_mag0);


                if (dist_mag0 < dist_ref && dist_mag0!=0){
                    texte = lst_mag_niveau0.get(r).toString();
                    mag = geomen.project(mag_niv0_geom[r], WKID_RGF93, mapRef);
                    dist_ref = dist_mag0;
                }
            }
            Log.d("mag",""+ mag);
            // Affichage du ppv :
            if (mag != null) {
                mGraphicsLayer.addGraphic(new Graphic(mag, new TextSymbol(taille, texte, color)));

                Log.d("niv0_mag!null", "Dedans");
            }

        } else if (niveau_dep == 1){
            Log.d("niv1","Dedans");

            Polygon buff_niv1 = geomen.buffer(depart, mapRef, distance_niv1, meter);
            Geometry magasin = geomen.intersect(buff_niv1, projection_mag_niv1, mapRef);

            for (int r=0; r<lst_mag_niveau1.size(); r++){
                Geometry mag_niv1_r = geomen.project(mag_niv1_geom[r], WKID_RGF93, mapRef);
                double dist_mag1 = geomen.distance(mag_niv1_r, magasin, mapRef);

                Log.d("magasin",""+magasin);
                Log.d("mag_niv1_r",""+mag_niv1_r);
                Log.d("dist_mag1",""+dist_mag1);


                // On cherhce le magasin le plus proche
                // c'est-à-dire à la distance minimale du point de départ
                if (dist_mag1 < dist_ref && dist_mag1!=0){
                    texte = lst_mag_niveau1.get(r).toString();
                    mag = geomen.project(mag_niv1_geom[r], WKID_RGF93, mapRef);
                    dist_ref = dist_mag1;
                }
            }
            Log.d("mag",""+ mag);
            // Affichage du ppv :
            if (mag != null) {
                mGraphicsLayer.addGraphic(new Graphic(mag, new TextSymbol(taille, texte, color)));

                Log.d("niv1_mag!null", "Dedans");
            }

        } else if (niveau_dep == 2){
            Log.d("niv2","Dedans");

            Polygon buff_niv2 = geomen.buffer(depart, mapRef, distance_niv2, meter);
            Geometry magasin = geomen.intersect(buff_niv2, projection_mag_niv2, mapRef);

            // On cherhce le magasin le plus proche
            // c'est-à-dire à la distance minimale du point de départ
            for (int r=0; r<lst_mag_niveau2.size(); r++){
                Geometry mag_niv2_r = geomen.project(mag_niv2_geom[r], WKID_RGF93, mapRef);
                double dist_mag2 = geomen.distance(mag_niv2_r, magasin, mapRef);

                Log.d("magasin",""+magasin);
                Log.d("mag_niv1_r",""+mag_niv2_r);
                Log.d("dist_mag1",""+dist_mag2);

                if (dist_mag2 < dist_ref && dist_mag2!=0){
                    texte = lst_mag_niveau2.get(r).toString();
                    mag = geomen.project(mag_niv2_geom[r], WKID_RGF93, mapRef);
                    dist_ref = dist_mag2;
                }
            }
            Log.d("mag",""+ mag);
            // Affichage du ppv :
            if (mag != null) {
                mGraphicsLayer.addGraphic(new Graphic(mag, new TextSymbol(taille, texte, color)));

                Log.d("niv2_mag!null", "Dedans");
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void popToast(final String message, final boolean show) {
        // Simple helper method for showing toast on the main thread
        if (!show)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
