

# Project "MediumCandy"

The color medium candy apple red, applied with a metallic sheen, is popular among car owners who customize their cars.

# Team Members

* Carlos Martínez Castillero (leader)
* Daniel García Paez
* Alberto Berbel Aznar
* Guillermo Ginestra Díaz

# Funcionalidades implementadas

A continuación, se dará una descripción de cada una de las funcionalidades que se han implementado en este proyecto.

## Servicio que almacena URLs alcanzables

Se ha implementado un Servicio que se encarga de comprobar que una URL es alcanzable previamente a ser acortada y almacenada en el sistema. Tal y como se acordó, el Servicio implementado se corresponde con el **Tipo 2 de implementación**.

El servicio principal que se encarga de responder a las peticiones se encuentra en el `MediumCandyController` y es accesible a través de la uri mostrada a continuación (siendo el parámetro `url` la URL que se desea acortar): 

- `http://URI_DEL_SERVIDOR/mediumcandy/linkreachable?url=URL` 

El servicio responderá con: 

- `CREATED (201)` en el caso de que la URL proporcionada sea alcanzable (y correctamente formada, por supuesto).
- `BAD REQUEST (400)` en el caso de que la URL que se desea acortar no sea alcanzable (o en su defecto, no se trate de una URL correctamente formada).
 
La funcionalidad más importante se encuentra implementada en el `UrlShortenerControllerWithLogs`, Servicio al que accede el `MediumCandyController` para poder dar respuesta a todas las peticiones que le llegan en `/mediumcandy/linkreachable`. Cabe destacar la función `private static boolean ping(String urlIn)` que determina si `urlIn` es una URL alcanzable. Aspectos destacables acerca de su implementación son:

- La utilización de la clase `HttpURLConnection` que permite realizar peticiones *http*.
- La existencia de un *timeout* de varios segundos, tras el cual si no hemos recibido respuesta tras realizar una petición cierra la conexión y determina que la URL dada no es alzanzable.
- En el caso de obtener respuesta accedemos a sus cabeceras *http* y comprobamos que el código recibido es válido y se trata por tanto de de una URL alcanzable.



## Acortamiento masivo de URLs mediante fichero CSV
Este servicio permite, a partir de un fichero en formato CSV con una url por línea, generar las urls acortadas de cada una de las urls en archivo CSV y devolver un nuevo fichero CSV que incluye las urls originales y las acortadas. Ha sido implementado siguiendo las características del **Nivel Tecnológico Tipo 2** como se indicó en la propuesta del proyecto.

El cliente debe realizar una llamada de tipo `POST` al servicio

- `http://URI_DEL_SERVIDOR/upload/` para subir el archivo CSV al servidor, realizar su tratamiento (convertir las urls en urls acortadas) y almacenarlo en el servidor para poder ser descargado.

Y una llamada `GET` al servicio

- `http://URI_DEL_SERVIDOR/files/{file_name}` que devolverá el nuevo archivo CSV con las urls acortadas ya incluídas.

La respuesta del servidor puede ser de dos tipos:

- Un stream de bytes en el `OuptpuStream` de la respuesta de tipo `HttpServletResponse` que se corresponde con el archivo CSV a descargar.
- `BAD REQUEST (400)` si la el formato del archivo CSV no es válido o si alguna de las urls del archivo no es válida o alcanzable.

El método encargado del tratamiento del fichero CSV, para acortar las urls incluidas en este hace uso del método  `shortenerIfReachable` de la clase `UrlShortenerControllerWithLogs` del proyecto `MediumCandy` para asegurarse de que las urls son válidas y alcanzables y entonces crearlas o no.

Se ha hecho uso de las clases del paquete `java.utils.concurrent` para que tanto las tareas de tratamiento del CSV como de descarga del mismo se puedan realizar de manera concurrente ante múltiples peticiones de varios clientes.

## Servicio de personalización de URLs acortadas

Este servicio permite añadir a la url acortada un texto cualquiera al final de la misma. Ha sido implementado siguiendo las características del **Nivel Tecnológico Tipo 2** como se indicó en la propuesta del proyecto.

El cliente debe realizar una llamada de tipo `POST` al servicio, indicando la `url` a acortar y el texto del `brand` deseado, a través de la url:

- `http://URI_DEL_SERVIDOR/mediumcandy/linkcustomized?url=URL&brand=BRAND`

La respuesta del servidor puede ser de dos tipos:

- `CREATED (201)` si la url introducida por parárámetro es válida y el brand no supera los 30 carácteres de extensión.
- `BAD REQUEST (400)` si la url no es válida y/o el brand supera los 30 carácteres.

Al realizar la llamada, el servidor accede al `MediumCandyController` que a su vez realiza una llamada `REST` al servicio implementado en `UrlShortenerControllerWithLogs`. El funcionamiento de este servicio es similar al que acorta la url sin que sea personalizada, las diferencias más significativas son las siguientes:

- Se ha redefinido el método `createAndSaveIfValid` de la clase `UrlShortenerController` del `common`, llamado `createAndSaveCustomizedIfValid`, la diferencia entre ambos métodos es que el `hash` en vez de ser aleatorio es el `brand` solicitado por el usuario. Además antes de comprobar si es válida la url larga, comprueba si ya existe ese brand en la base de datos. En el caso de que así sea, modifica esa `ShortURL` para que el `target` asociado a ese hash (brand) sea la nueva url larga, es decir, solo puede haber una **única** url corta para cada `brand`.
- Además, hizo falta ampliar el tamaño del `VARCHAR` del `hash` en la base de datos para esta funcionalidad.

## Información y estadísticas de URLs

Funcionalidad que consiste en mostrar estadísticias de las URLs almacenadas en la Base de Datos con su URL corta implementado con **nivel tecnológico Tipo 2** como acordamos. 

Cada vez que alguien accede a una dirección con una URL corta, se almacena en la Base de Datos. Para conocer las estadísticas de una dirección basta con hacer una petición get a la siguiente dirección del servidor, donde URL es la dirección del cual se quieren obtener las estadísticias e información: 

- `http://URI_DEL_SERVIDOR/mediumcandy/linkstats?url=URL`  

Esta dirección accede al `MediumCandyController` la cual a su vez llama a `UrlShortenerControllerWithLogs`. Este controller se encarga de acceder a la Base de Datos para buscar el objeto ShortURL, y con el hash de este recuperar de la Base de Datos el número de clicks (veces que se a accedido a la dirección dada por parámetro). 

Este método devuelve lo siguiente: 

- `OK (200)` junto con la lista de las estadísticias, en el caso de que todo haya ido bien y haya en la Base de Datos una ShortURL con la dirección dada.  
- `BAD REQUEST (400)` en el caso de que la URL dada no este en la Base de Datos