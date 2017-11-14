<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/analysis/testStrategy">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="classes.css" />
        <link rel="stylesheet" type="text/css" href="elements.css" />
        <script src="jquery.js"></script>
        <script src="script.js"></script>
      </head>

      <body>
        <h1>Analysis Report</h1>
        <div>
        <h2 id="eheader" onclick="toggle(this, '#eventtable');">
          Events
        </h2>
        <table id="eventtable" name="eventtable" >
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Screenshot</th>
          </tr>
          <xsl:for-each select="events/event">
            <tr>
              <td style="text-align:center">
                <a name="{@id}"></a>
                <xsl:value-of select="@id"/>
              </td>
              <td><xsl:value-of select="name"/></td>
              <td><xsl:value-of select="description"/></td>
              <td style="text-align:center">
                <xsl:if test="@sid!=''">
                  <a href="#" onclick="showImage('{@sid}');return false;">View</a>
                </xsl:if>
              </td>
            </tr>
          </xsl:for-each>
        </table>
        <h2 class="table-bottom"> </h2>

        <xsl:for-each select="actionset/actions">
          <xsl:variable name="tablename">
            <xsl:choose>
              <xsl:when test="@type='file'">filetable</xsl:when>
              <xsl:when test="@type='network'">networktable</xsl:when>
              <xsl:when test="@type='sms'">smstable</xsl:when>
              <xsl:when test="@type='url'">urltable</xsl:when>
            </xsl:choose>
          </xsl:variable>

          <xsl:if test="@type='file'">
            <h2 onclick="toggle(this, '#filetable');">
              File Actions
            </h2>
          </xsl:if>
          <xsl:if test="@type='network'">
            <h2 onclick="toggle(this, '#networktable');">
              Network Actions
            </h2>
          </xsl:if>
          <xsl:if test="@type='sms'">
            <h2 onclick="toggle(this, '#smstable');">
              SMS Actions
            </h2>
          </xsl:if>
          <xsl:if test="@type='url'">
            <h2 onclick="toggle(this, '#urltable');">
              URL Actions
            </h2>
          </xsl:if>

          <table>
            <xsl:attribute name="id">
              <xsl:value-of select="$tablename" />
            </xsl:attribute>
            <tr>
              <th width="10%">Event</th>
              <xsl:if test="@type='file'">
                <th>File Path</th>
              </xsl:if>
              <xsl:if test="@type='network'">
                <th>Address</th>
                <th>Port</th>
              </xsl:if>
              <xsl:if test="@type='sms'">
                <th>Destination</th>
                <th>Text data</th>
              </xsl:if>
              <xsl:if test="@type='url'">
                <th>Server</th>
              </xsl:if>
            </tr>
            <xsl:for-each select="action">
              <tr>
                <td>
                  <a href="{event}" onclick="$('#eventtable').show();
                    $('#eheader').css('border-radius', '20px 20px 0px 0px');">
                    View Event
                  </a>
                </td>
                <xsl:if test="../@type='file'">
                  <td><xsl:value-of select="filepath"/></td>
                </xsl:if>
                <xsl:if test="../@type='network'">
                  <td><xsl:value-of select="address"/></td>
                  <td><xsl:value-of select="port"/></td>
                </xsl:if>
                <xsl:if test="../@type='sms'">
                  <td><xsl:value-of select="destination"/></td>
                  <td><xsl:value-of select="text"/></td>
                </xsl:if>
                <xsl:if test="../@type='url'">
                  <td><xsl:value-of select="server"/></td>
                </xsl:if>
              </tr>
            </xsl:for-each>
          </table>
          <h2 class="table-bottom"> </h2>
        </xsl:for-each>
        </div>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>
