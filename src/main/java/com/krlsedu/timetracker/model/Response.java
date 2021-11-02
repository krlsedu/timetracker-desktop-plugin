/* ==========================================================
File:        Dependencies.java
Description: Manages plugin dependencies.
Maintainer:  WakaTime <support@wakatime.com>
License:     BSD, see LICENSE for more details.
Website:     https://wakatime.com/
===========================================================*/

package com.krlsedu.timetracker.model;


public class Response {
    public int statusCode;
    public String body;
    public String etag;
    
    public Response(int statusCode, String body, String etag) {
        this.statusCode = statusCode;
        this.body = body;
        this.etag = etag;
    }
}

