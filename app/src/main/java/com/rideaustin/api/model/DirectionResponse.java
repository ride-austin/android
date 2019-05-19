package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by e.shloma on 10.07.16.
 */

public class DirectionResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("routes")
    private List<Routes> routes;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Routes> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Routes> routes) {
        this.routes = routes;
    }

    public class Routes {

        @SerializedName("summary")
        private String summary;

        @SerializedName("legs")
        private List<Legs> legs;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<Legs> getLegs() {
            return legs;
        }

        public void setLegs(List<Legs> legs) {
            this.legs = legs;
        }

        public class Legs {

            @SerializedName("duration")
            private Duration duration;

            @SerializedName("steps")
            private List<Steps> steps;

            @SerializedName("distance")
            private Distance distance;

            public List<Steps> getSteps() {
                return steps;
            }

            public void setSteps(List<Steps> steps) {
                this.steps = steps;
            }

            public Duration getDuration() {
                return duration;
            }

            public void setDuration(Duration duration) {
                this.duration = duration;
            }

            public Distance getDistance() {
                return distance;
            }

            public class Distance extends Duration {
            }

            public class Duration {
                @SerializedName("text")
                private String text;

                @SerializedName("value")
                private String value;

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public class Steps {

                @SerializedName("polyline")
                private Polyline polyline;

                @SerializedName("duration")
                private Duration duration;

                public Polyline getPolylines() {
                    return polyline;
                }

                public void setPolylines(Polyline polylines) {
                    this.polyline = polylines;
                }

                public Polyline getPolyline() {
                    return polyline;
                }

                public void setPolyline(Polyline polyline) {
                    this.polyline = polyline;
                }

                public Duration getDuration() {
                    return duration;
                }

                public void setDuration(Duration duration) {
                    this.duration = duration;
                }

                public class Polyline {

                    @SerializedName("points")
                    private String poitns;

                    public String getPoitns() {
                        return poitns;
                    }

                    public void setPoitns(String poitns) {
                        this.poitns = poitns;
                    }
                }
            }
        }
    }

}

/**
 * Don't delete, because we also need destination time
  */

//{
//        "status": "OK",
//        "geocoded_waypoints" : [
//        {
//        "geocoder_status" : "OK",
//        "place_id" : "ChIJ7cv00DwsDogRAMDACa2m4K8",
//        "types" : [ "locality", "political" ]
//        },
//        {
//        "geocoder_status" : "OK",
//        "place_id" : "ChIJ69Pk6jdlyIcRDqM1KDY3Fpg",
//        "types" : [ "locality", "political" ]
//        },
//        {
//        "geocoder_status" : "OK",
//        "place_id" : "ChIJgdL4flSKrYcRnTpP0XQSojM",
//        "types" : [ "locality", "political" ]
//        },
//        {
//        "geocoder_status" : "OK",
//        "place_id" : "ChIJE9on3F3HwoAR9AhGJW_fL-I",
//        "types" : [ "locality", "political" ]
//        }
//        ],
//        "routes": [ {
//        "summary": "I-40 W",
//        "legs": [ {
//        "steps": [ {
//        "travel_mode": "DRIVING",
//        "start_location": {
//        "lat": 41.8507300,
//        "lng": -87.6512600
//        },
//        "end_location": {
//        "lat": 41.8525800,
//        "lng": -87.6514100
//        },
//        "polyline": {
//        "points": "a~l~Fjk~uOwHJy@P"
//        },
//        "duration": {
//        "value": 19,
//        "text": "1 min"
//        },
//        "html_instructions": "Head \u003cb\u003enorth\u003c/b\u003e on \u003cb\u003eS Morgan St\u003c/b\u003e toward \u003cb\u003eW Cermak Rd\u003c/b\u003e",
//        "distance": {
//        "value": 207,
//        "text": "0.1 mi"
//        }
//        },
//        ...
//        ... additional steps of this leg
//        ...
//        ... additional legs of this route
//        "duration": {
//        "value": 74384,
//        "text": "20 hours 40 mins"
//        },
//        "distance": {
//        "value": 2137146,
//        "text": "1,328 mi"
//        },
//        "start_location": {
//        "lat": 35.4675602,
//        "lng": -97.5164276
//        },
//        "end_location": {
//        "lat": 34.0522342,
//        "lng": -118.2436849
//        },
//        "start_address": "Oklahoma City, OK, USA",
//        "end_address": "Los Angeles, CA, USA"
//        } ],
//        "copyrights": "Map data ©2010 Google, Sanborn",
//        "overview_polyline": {
//        "points": "a~l~Fjk~uOnzh@vlbBtc~@tsE`vnApw{A`dw@~w\\|tNtqf@l{Yd_Fblh@rxo@b}@xxSfytAblk@xxaBeJxlcBb~t@zbh@jc|Bx}C`rv@rw|@rlhA~dVzeo@vrSnc}Axf]fjz@xfFbw~@dz{A~d{A|zOxbrBbdUvpo@`cFp~xBc`Hk@nurDznmFfwMbwz@bbl@lq~@loPpxq@bw_@v|{CbtY~jGqeMb{iF|n\\~mbDzeVh_Wr|Efc\\x`Ij{kE}mAb~uF{cNd}xBjp]fulBiwJpgg@|kHntyArpb@bijCk_Kv~eGyqTj_|@`uV`k|DcsNdwxAott@r}q@_gc@nu`CnvHx`k@dse@j|p@zpiAp|gEicy@`omFvaErfo@igQxnlApqGze~AsyRzrjAb__@ftyB}pIlo_BflmA~yQftNboWzoAlzp@mz`@|}_@fda@jakEitAn{fB_a]lexClshBtmqAdmY_hLxiZd~XtaBndgC"
//        },
//        "warnings": [ ],
//        "waypoint_order": [ 0, 1 ],
//        "bounds": {
//        "southwest": {
//        "lat": 34.0523600,
//        "lng": -118.2435600
//        },
//        "northeast": {
//        "lat": 41.8781100,
//        "lng": -87.6297900
//        }
//        }
//        } ]
//        }