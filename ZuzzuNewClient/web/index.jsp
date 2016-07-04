<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cultbay</title>
    </head>

    <body>

        <h1>Cultbay Request</h1>
        <!--<form action="http://89.202.135.54:8080/CultSwitchProxy/HSServlet" method="post">-->
        <form action="CultBayImpl" method="post">
            <textarea name="Cultbay_RQ" rows="20" cols="80">
<?xml version='1.0' encoding='UTF-8'?> 
<Cultbay_Request>
<Location>
<Country>Belgium</Country>
<Region></Region>
<Place></Place>
</Location>
</Cultbay_Request>

<!--<?xml version='1.0' encoding='UTF-8'?>
<Cultbay_GeoTagsRequest>
</Cultbay_GeoTagsRequest>-->


<!--<?xml version="1.0" encoding="UTF-8"?>
<Cultbay_AuctionDetailsRQ>
<AuctionID>180986846221</AuctionID>
</Cultbay_AuctionDetailsRQ>-->

<!--<?xml version='1.0' encoding='UTF-8'?>
<UpdateCountriesRegionsPlacesRQ>
</UpdateCountriesRegionsPlacesRQ>-->


<!--<?xml version="1.0" encoding="UTF-8"?>
<CurrentPriceBidCountRQ>
<ItemId>180986846221</ItemId> 
</CurrentPriceBidCountRQ>-->
            </textarea><br>
            <input type="submit" value="Send Request"/>
            <input type="reset"/>
        </form>

        <%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%>
        <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
        --%>

    </body>
</html>
