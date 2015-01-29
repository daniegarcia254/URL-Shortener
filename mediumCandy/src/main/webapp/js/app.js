/******************************************************************************
 *                    A P P   V a r i a b l e s
 *****************************************************************************/
var SERVICE_URI = "http://localhost:8080/";

/* Alert Messages */
var ALERT_SHORTEN_URL = "Unable to shorten that link. It is not a valid or reachable url.";
var ALERT_ALREADY_SHORTEN = "That is already a shortened link!";
var ALERT_STATS = "Could not show the stats of that link. Try with a different one.";
var ALERT_BRANDED = "Unable to shorten that link. Check out your form or try a different brand.";

/* Other vars */
var shortenedUriList = []; // list of shortened urls objects
var menuSelected = "shortener"; // last menu selected option



/******************************************************************************
 *                  A P P   F u n c t i o n a l i t y
 *****************************************************************************/
/* Document Ready Functionality (jQuery stuff) */
$( document ).ready(function() { 
  setUrlSubmition();
  setBrandedSubmition();
  setStatsSubmition();
  setUploadSubmition();
  setMenuCallbacks();
});

/*
 * This function is called everytime the user submits the form.
 */
function setUrlSubmition() {
  // jQuery way!
  $( '#urlShortenerForm' ).on('submit', function (e) {
    var url = getUrl();  
    
    if ( ! emptyUserInput(url) ) {
      shortenURL(url);
    }
    
    e.preventDefault(); //stop form submission
  });
}

/*
 * This function is called everytime the user submits the form.
 */
function setBrandedSubmition() {
  // jQuery way!
  $( '#urlBrandedForm' ).on('submit', function (e) {
    var url = getBrandedUrl();
    var brand = getBrand();
    
    if ( ! emptyUserInput(url) && !  emptyUserInput(brand) ) {
      brandedURL(url, brand)
    }
    
    e.preventDefault(); //stop form submission
  });
}

/*
 * This function is called everytime the user submits the form.
 */
function setStatsSubmition() {  
  // jQuery way!
  $( '#urlStatsForm' ).on('submit', function (e) {
    var url = getUrlStats();  
    
    if ( ! emptyUserInput(url) ) {
      // AJAX CALL
      showStats(url);
    }
    
    e.preventDefault(); //stop form submission
  });
}

/*
 * This function is called everytime the user submits the form.
 */
function setUploadSubmition() {
  // here comes the code
  $( '#fileUploader' ).uploadFile({
    url: SERVICE_URI + "upload",
    allowedTypes:"csv",
    multiple:false,
    fileName:"myfile"
  });
}

/*
 * Set's up the functionality to make the page dynamically navigable.
 */
function setMenuCallbacks() {
  $( '#link-shortener' ).on('click', function (e) {
    updateMenu('#link-shortener');
    updatePage('#shortener');
    menuSelected = "shortener";

    e.preventDefault();
  });
  
  $( '#link-csv' ).on('click', function (e) {
    updateMenu('#link-csv');
    updatePage('#csv');
    menuSelected = "csv";

    e.preventDefault();
  });
  
  $( '#link-branded' ).on('click', function (e) {
    updateMenu('#link-branded');
    updatePage('#branded');
    menuSelected = "branded";

    e.preventDefault();
  });
  
  $( '#link-stats' ).on('click', function (e) {
    updateMenu('#link-stats');
    updatePage('#stats');
    menuSelected = "stats";

    e.preventDefault();
  });
}

/*
 * Changes the menu link colors dynamically.
 */
function updateMenu(linkId) {
  var oldLinkId = '#link-' + menuSelected;
  // update menu css
  $( oldLinkId ).css( "color", "#FFF");
  $( linkId ).css( "color", "#E2062C");
}

/*
 * Hides the page showing and dynamically shows the new page
 * selected on the menu bar.
 */
function updatePage(pageId) {
  var oldPageId = '#' + menuSelected;
  if ( pageId != "#" + menuSelected ) {
    $( oldPageId ).hide();
    $( pageId ).slideDown();
  }
}

/*
 * Returns the URL the user entered.
 */
function getUrl() {
  return $( '#urlInput' ).val();
}

/*
 * Returns the URL the user entered.
 */
function getBrandedUrl() {
  return $( '#urlBrandInput' ).val();
}

/*
 * Returns the brand the user entered.
 */
function getBrand() {
  return $( '#brandInput' ).val();
}

/*
 * Returns the URL the user entered.
 */
function getUrlStats() {
  return $( '#urlStatsInput' ).val();
}

/*
 * Sets the content of the '#urlInput' input to the shortened
 * uri contained inside the 'objectUri' object.
 */
function setUrlInput(objectUri) {
  var shortenedUri = objectUri.uri;
  $( '#urlInput' ).val( shortenedUri );
}

/*
 * Clears all text in #urlInput.
 */
function clearUrlInput() {
  $( '#urlInput' ).val('');
}

/*
 * Clears all text in #urlStatsInput.
 */
function clearStatsInput() {
  $( '#urlStatsInput' ).val('');
}

/*
 * Clears all text in #urlBrandedForm.
 */
function clearBrandedInput() {
  $( '#brandInput' ).val('');
  $( '#urlBrandInput' ).val('');
}

/*
 * Returns TRUE if 'input' is an empty String.
 */
function emptyUserInput(input) {
  return input == "";
}

/* 
 * Shows the alert box with the given 'alertMessage' text in it, and 
 * automatically hides after 5 seconds - if isn't already visible.
 */
function showAlert(alertMessage) {
  if ( elementIsVisible( '#alert-box' ) ) {
    $( '#alert-box' ).html(alertMessage);
    $( '#alert-box' ).slideDown().delay(5000).slideUp();
  }
}

function showSuccess(successMessage) {
  if ( elementIsVisible( '#success-box' ) ) {
    $( '#success-box' ).html(successMessage);
    $( '#success-box' ).slideDown().delay(5000).slideUp();
  }
}

/*
 * Returns TRUE if the element 'element' is visible in the DOM, and
 * FALSE if it is hidden.
 */
function elementIsVisible(element) {
  return $( element ).css('display') == 'none';
}

/*
 * Shows the given 'objUri' object in the DOM.
 */
function showShortenedUri(objUri) {
  var shortUri = objUri.uri;
  var targetUri = objUri.target;
  var shortenedUri = {uri: shortUri, target:targetUri};
  
  // DOM insertion
  insertShortenedUriInDOM(shortenedUri);
}

/* 
 * Inserts the given shortenedUri object in the DOM.
 */
function insertShortenedUriInDOM(shortenedUri) {
  // 1. Insert to latest uri details block
  insertLatestShortenedUriInDOM(shortenedUri);
  
  // 2. If there were other uris shortened before, prepend the one
  //    shown in latest uri details block to the list of shortened uris
  if ( shortenedUriList.length > 0 ) {
    // if the list is empty, it will be hidden, we have to show it
    if ( shortenedUriList.length == 1 ) {
      $( '.shortened-url-block' ).show();
    }
    // 2.1. get first array elem (we need it!)
    var latest = shortenedUriList.shift();
    // and insert it where it was ('shift()' method deleted it! from the array)
    shortenedUriList.unshift(latest);
    console.log(latest);
    // 2.2. update the shortened uris list (DOM)
    prependLatestShortenedUriInDOM(latest);
  }
  
  // 3. add new shortened uri to the array of shortened uris
  shortenedUriList.unshift(shortenedUri);
}

/*
 * Inserts the HTML of the given shortenedUri object in the DOM, inside
 * the list of already shortened URLs.
 */
function prependLatestShortenedUriInDOM(shortenedUri) {
  var uri = $(  '<div class="shorten-url-elem">' +
                  '<div class="img-block">' +
                    '<img src="/img/href_small.png">' +
                  '</div>' +
                  '<div class="details-block">' +
                    '<div class="shortened-url">' + shortenedUri.uri + '</div>' +
                    '<div class="target-url"><a target="_blank" href="' + shortenedUri.target + '">' + shortenedUri.target + '</a></div>' +
                  '</div>' +
                '</div><br>');
  
  $( '.shortened-url-block' ).prepend( uri );
}

/*
 * Inserts the HTML of the given shortenedUri object, inside
 * the latest shortened URL block.
 */
function insertLatestShortenedUriInDOM(shortenedUri) {
  var uri = $(  '<div class="shorten-url-elem">' +
                  '<div class="img-block">' +
                    '<img src="/img/href.png">' + 
                  '</div>' +
                  '<div class="details-block">' +
                    '<div class="shortened-url">' + shortenedUri.uri + '</div>' +
                    '<div class="target-url"><a target="_blank" href="' + shortenedUri.target + '">' + shortenedUri.target + '</a></div>' +
                  '</div>' +
                '</div>');
  
  $( '#shorten-block' ).html( uri );
  // animation when shown! :-)
  $( '#shorten-block' ).hide();
  $( '#shorten-block' ).slideDown();
}

/*
 * Inserts the HTML of the given shortenedUri object, inside
 * the latest shortened URL block.
 */
function insertStatsInDOM(statsObj) {
  var stats = $(  '<div class="shorten-url-elem">' +
            '<div class="img-block">' +
              '<img src="/img/stats.png">' +
              '<div class="clicks-url">' +
                '<span>' + statsObj.numClicks + '</span> clicks</div>' +
            '</div>' +
            '<div class="details-block">' +
              '<div class="owner-url"><span>owner:</span> ' + statsObj.owner + '</div>' +
              '<div class="group">' +
                '<div class="target-url">' +
                  '<a target="_blank" href="' + statsObj.target + '">' + statsObj.target + '</a>' +
                '</div>' +
              '</div>' +
            '</div>' +
          '</div>' );
  
  $( '.stats-url-block' ).html( stats );
  // animation when shown! :-)
  $( '.stats-url-block' ).hide();
  $( '.stats-url-block' ).slideDown();
}

/*
 * Inserts the HTML of the given brandedUri object, inside
 * the latest shortened URL block.
 */
function insertBrandedUriInDOM(brandedObj) {
  var branded = $(  '<div class="shorten-url-elem">' +
                      '<div class="img-block">' +
                        '<img src="/img/href.png">' +
                      '</div>' +
                      '<div class="details-block">' +
                        '<div class="shortened-url">' + brandedObj.uri + '</div>' +
                        '<div class="target-url"><a target="_blank" href="' 
                        + brandedObj.target + '">' + brandedObj.target + '</a>' +
                        '</div>' +
                      '</div>' +
                    '</div>' );
  
  $( '#branded-block' ).html( branded );
  // animation when shown! :-)
  $( '#branded-block' ).hide();
  $( '#branded-block' ).slideDown();
}

/*
 * Selects the content of the #urlInput input.
 */
function selectUserInput() {
  $( '#urlInput' ).select();
}

/*
 * Selects the content of the #urlStatsInput input.
 */
function selectStatsInput() {
  $( '#urlStatsInput' ).select();
}

/*
 * Returns TRUE if the given URL 'url' is already a shortened link,
 * and TRUE if it is not.
 */
function isShortenUri(url) {
  var subUri = url.substring(0, 22);
  return subUri == SERVICE_URI;
}



/******************************************************************************
 *                           A P I   C a l l s
 *****************************************************************************/
function downloadFile(fileName) {
  setTimeout(function(){
    window.location.href = SERVICE_URI + "files/" + fileName;
  },4000);
}

function showStats(url) {
  $.ajax({
    url : SERVICE_URI + "mediumcandy/linkstats?url=" + url,
	contentType : 'application/json',
	type : 'GET',
	success : function (response)
	{
      console.log("exito");
      var statsObj = response[0];
      insertStatsInDOM(statsObj);
      clearStatsInput();
	},
	error: function (error) {
      console.log("Oops! Error: " + error);
      showAlert(ALERT_STATS);
      selectStatsInput();
	}
  });
}

/*
 * ( POST method ): Shortens the given URL.
 */
function shortenURL(url) {
  $.ajax({
    type : 'POST',
	contentType : 'application/json',
	url : SERVICE_URI + "mediumcandy/linkreachable?url=" + url,
	dataType : "json",
	success : function(response) {
      // update DOM with response data
      showShortenedUri(response);
      // things to do after call
      setUrlInput(response);
      selectUserInput();
      console.log('exito!');
    },    
    error : function(error) {
      if ( isShortenUri(url) ) {
        showAlert(ALERT_ALREADY_SHORTEN);
      } else {
        showAlert(ALERT_SHORTEN_URL);
      }
      console.log("Oops! RESPONSE Status:  " + error.status);
    }
  });
}

/*
 * ( POST method ): Shortens the given URL.
 */
function brandedURL(url, brand) {
  $.ajax({
    type : 'POST',
	contentType : 'application/json',
	url : SERVICE_URI + "mediumcandy/linkcustomized?url=" + url + "&brand=" + brand,
	dataType : "json",
	success : function(response) {
      var brandedObj = response;
      // update DOM with response data
      insertBrandedUriInDOM(brandedObj)
      // clear form
      clearBrandedInput();
      console.log('exito!');
    },    
    error : function(error) {
      showAlert(ALERT_BRANDED);
      console.log("Oops! RESPONSE Status:  " + error.status);
    }
  });
}