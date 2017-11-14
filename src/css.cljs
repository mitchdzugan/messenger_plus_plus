(ns extension.css)

(def my-css
  "
progress {
  vertical-align: baseline;
}

@-webkit-keyframes progress-bar-stripes {
  from {
    background-position: 1rem 0;
  }
  to {
    background-position: 0 0;
  }
}

@keyframes progress-bar-stripes {
  from {
    background-position: 1rem 0;
  }
  to {
    background-position: 0 0;
  }
}

.progress {
  display: -ms-flexbox;
  display: flex;
  overflow: hidden;
  font-size: 0.75rem;
  line-height: 1rem;
  text-align: center;
  background-color: #e9ecef;
  border-radius: 0.25rem;
}

.progress-bar {
  height: 1rem;
  line-height: 1rem;
  color: #fff;
  background-color: #007bff;
  transition: width 0.6s ease;
}

.progress-bar-striped {
  background-image: linear-gradient(45deg, rgba(255, 255, 255, 0.15) 25%, transparent 25%, transparent 50%, rgba(255, 255, 255, 0.15) 50%, rgba(255, 255, 255, 0.15) 75%, transparent 75%, transparent);
  background-size: 1rem 1rem;
}

.progress-bar-animated {
  -webkit-animation: progress-bar-stripes 1s linear infinite;
          animation: progress-bar-stripes 1s linear infinite;
}

  .display-flex {
     display: -webkit-flex;
     display: flex;
   }

[data-draggable=\"target\"]
{
	float:left;
	list-style-type:none;
	overflow-y:auto;
	margin:0 0.5em 0.5em 0;
	padding:0.5em;
}

/* drop target state */
[data-draggable=\"target\"][aria-dropeffect=\"move\"]
{
	border-color:#68b;

	background:#fff;
}

/* drop target focus and dragover state */
[data-draggable=\"target\"][aria-dropeffect=\"move\"]:focus,
[data-draggable=\"target\"][aria-dropeffect=\"move\"].dragover
{
	outline:none;
	box-shadow:0 0 0 1px #fff, 0 0 0 3px #68b;
}

/* draggable items */
[data-draggable=\"item\"]
{
	display:block;
	list-style-type:none;
	margin:0 0 2px 0;
	padding:0.2em 0.4em;
	border-radius:0.2em;
	line-height:1.3;
}

/* items focus state */
[data-draggable=\"item\"]:focus
{
	outline:none;
	box-shadow:0 0 0 2px #68b, inset 0 0 0 1px #ddd;
}

/* items grabbed state */
[data-draggable=\"item\"][aria-grabbed=\"true\"]
{
	background:#0084ff;
	color:#fff;
}

/*----------------------------------------------------------------------------------------
  The section immediately below is required for the drop-down components and comes from
  the bootstrap-chosen library:
    https://github.com/alxlit/bootstrap-chosen
    License: MIT.
  That library provides it's css as a .less file.
  To compile the less file into the css below, use the following steps from
  a command prompt (you'll need git and the lessc compiler (http://lesscss.org):
	git clone https://github.com/alxlit/bootstrap-chosen
	cd bootstrap-chosen
	git clone --depth=1 https://github.com/twbs/bootstrap
  Now edit bootstrap-chosen.less and add the following two lines before the first @import:
    lessc bootstrap-chosen.less > bootstrap-chosen.css
  Finally, replace the section below with the contents of bootstrap-chosen.css.
  START OF BOOTSTRAP-CHOSEN SECTION...
----------------------------------------------------------------------------------------*/

.chosen-select {
  width: 100%;
}
.chosen-select-deselect {
  width: 100%;
}
.chosen-container {
  display: inline-block;
  font-size: 14px;
  position: relative;
  vertical-align: middle;
}
.chosen-container .chosen-drop {
  background: #ffffff;
  border: 1px solid #cccccc;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  -webkit-box-shadow: 0 8px 8px rgba(0, 0, 0, .25);
  box-shadow: 0 8px 8px rgba(0, 0, 0, .25);
  margin-top: -1px;
  position: absolute;
  top: 100%;
  left: -9000px;
  z-index: 1060;
}
.chosen-container.chosen-with-drop .chosen-drop {
  left: 0;
  right: 0;
}
.chosen-container .chosen-results {
  color: #555555;
  margin: 0 4px 4px 0;
  max-height: 240px;
  padding: 0 0 0 4px;
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.chosen-container .chosen-results li {
  display: none;
  line-height: 1.42857143;
  list-style: none;
  margin: 0;
  padding: 5px 6px;
}
.chosen-container .chosen-results li em {
  background: #feffde;
  font-style: normal;
}
.chosen-container .chosen-results li.group-result {
  display: list-item;
  cursor: default;
  color: #999;
  font-weight: bold;
}
.chosen-container .chosen-results li.group-option {
  padding-left: 15px;
}
.chosen-container .chosen-results li.active-result {
  cursor: pointer;
  display: list-item;
}
.chosen-container .chosen-results li.highlighted {
  background-color: #428bca;
  background-image: none;
  color: white;
}
.chosen-container .chosen-results li.highlighted em {
  background: transparent;
}
.chosen-container .chosen-results li.disabled-result {
  display: list-item;
  color: #777777;
}
.chosen-container .chosen-results .no-results {
  background: #eeeeee;
  display: list-item;
}
.chosen-container .chosen-results .error {
  text-align: center;
  background: #f2dede;
  display: list-item;
}
.chosen-container .chosen-results .loading {
  text-align: center;
  color: #ccc;
  display: list-item;
}
.chosen-container .chosen-results-scroll {
  background: white;
  margin: 0 4px;
  position: absolute;
  text-align: center;
  width: 321px;
  z-index: 1;
}
.chosen-container .chosen-results-scroll span {
  display: inline-block;
  height: 1.42857143;
  text-indent: -5000px;
  width: 9px;
}
.chosen-container .chosen-results-scroll-down {
  bottom: 0;
}
.chosen-container .chosen-results-scroll-down span {
  background: url(\"chosen-sprite.png\") no-repeat -4px -3px;
}
.chosen-container .chosen-results-scroll-up span {
  background: url(\"chosen-sprite.png\") no-repeat -22px -3px;
}
.chosen-container-single .chosen-single {
  background-color: #ffffff;
  -webkit-background-clip: padding-box;
  -moz-background-clip: padding;
  background-clip: padding-box;
  border: 1px solid #cccccc;
  border-top-right-radius: 4px;
  border-top-left-radius: 4px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  color: #555555;
  display: block;
  height: 34px;
  overflow: hidden;
  line-height: 34px;
  padding: 0 0 0 8px;
  position: relative;
  text-decoration: none;
  white-space: nowrap;
}
.chosen-container-single .chosen-single span {
  display: block;
  margin-right: 26px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chosen-container-single .chosen-single abbr {
  background: url(\"chosen-sprite.png\") right top no-repeat;
  display: block;
  font-size: 1px;
  height: 10px;
  position: absolute;
  right: 26px;
  top: 12px;
  width: 12px;
}
.chosen-container-single .chosen-single abbr:hover {
  background-position: right -11px;
}
.chosen-container-single .chosen-single.chosen-disabled .chosen-single abbr:hover {
  background-position: right 2px;
}
.chosen-container-single .chosen-single div {
  display: block;
  height: 100%;
  position: absolute;
  top: 0;
  right: 0;
  width: 18px;
}
.chosen-container-single .chosen-single div b {
  background: url(\"chosen-sprite.png\") no-repeat 0 7px;
  display: block;
  height: 100%;
  width: 100%;
}
.chosen-container-single .chosen-default {
  color: #777777;
}
.chosen-container-single .chosen-search {
  margin: 0;
  padding: 3px 4px;
  position: relative;
  white-space: nowrap;
  z-index: 1000;
}
.chosen-container-single .chosen-search input[type=\"text\"] {
  background: url(\"chosen-sprite.png\") no-repeat 100% -20px, #ffffff;
  border: 1px solid #cccccc;
  border-top-right-radius: 4px;
  border-top-left-radius: 4px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  margin: 1px 0;
  padding: 4px 20px 4px 4px;
  width: 100%;
}
.chosen-container-single .chosen-drop {
  margin-top: -1px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  -webkit-background-clip: padding-box;
  -moz-background-clip: padding;
  background-clip: padding-box;
}
.chosen-container-single-nosearch .chosen-search input {
  position: absolute;
  left: -9000px;
}
.chosen-container-multi .chosen-choices {
  background-color: #ffffff;
  border: 1px solid #cccccc;
  border-top-right-radius: 4px;
  border-top-left-radius: 4px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  cursor: text;
  height: auto !important;
  height: 1%;
  margin: 0;
  overflow: hidden;
  padding: 0;
  position: relative;
}
.chosen-container-multi .chosen-choices li {
  float: left;
  list-style: none;
}
.chosen-container-multi .chosen-choices .search-field {
  margin: 0;
  padding: 0;
  white-space: nowrap;
}
.chosen-container-multi .chosen-choices .search-field input[type=\"text\"] {
  background: transparent !important;
  border: 0 !important;
  -webkit-box-shadow: none;
  box-shadow: none;
  color: #555555;
  height: 32px;
  margin: 0;
  padding: 4px;
  outline: 0;
}
.chosen-container-multi .chosen-choices .search-field .default {
  color: #999;
}
.chosen-container-multi .chosen-choices .search-choice {
  -webkit-background-clip: padding-box;
  -moz-background-clip: padding;
  background-clip: padding-box;
  background-color: #eeeeee;
  border: 1px solid #cccccc;
  border-top-right-radius: 4px;
  border-top-left-radius: 4px;
  border-bottom-right-radius: 4px;
  border-bottom-left-radius: 4px;
  background-image: -webkit-linear-gradient(top, #ffffff 0%, #eeeeee 100%);
  background-image: -o-linear-gradient(top, #ffffff 0%, #eeeeee 100%);
  background-image: linear-gradient(to bottom, #ffffff 0%, #eeeeee 100%);
  background-repeat: repeat-x;
  filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ffffffff', endColorstr='#ffeeeeee', GradientType=0);
  -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
  color: #333333;
  cursor: default;
  line-height: 13px;
  margin: 6px 0 3px 5px;
  padding: 3px 20px 3px 5px;
  position: relative;
}
.chosen-container-multi .chosen-choices .search-choice .search-choice-close {
  background: url(\"chosen-sprite.png\") right top no-repeat;
  display: block;
  font-size: 1px;
  height: 10px;
  position: absolute;
  right: 4px;
  top: 5px;
  width: 12px;
  cursor: pointer;
}
.chosen-container-multi .chosen-choices .search-choice .search-choice-close:hover {
  background-position: right -11px;
}
.chosen-container-multi .chosen-choices .search-choice-focus {
  background: #d4d4d4;
}
.chosen-container-multi .chosen-choices .search-choice-focus .search-choice-close {
  background-position: right -11px;
}
.chosen-container-multi .chosen-results {
  margin: 0 0 0 0;
  padding: 0;
}
.chosen-container-multi .chosen-drop .result-selected {
  display: none;
}
.chosen-container-active .chosen-single {
  border: 1px solid #66afe9;
  -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  -webkit-transition: border linear .2s, box-shadow linear .2s;
  -o-transition: border linear .2s, box-shadow linear .2s;
  transition: border linear .2s, box-shadow linear .2s;
}
.chosen-container-active.chosen-with-drop .chosen-single {
  background-color: #ffffff;
  border: 1px solid #66afe9;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
  -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  -webkit-transition: border linear .2s, box-shadow linear .2s;
  -o-transition: border linear .2s, box-shadow linear .2s;
  transition: border linear .2s, box-shadow linear .2s;
}
.chosen-container-active.chosen-with-drop .chosen-single div {
  background: transparent;
  border-left: none;
}
.chosen-container-active.chosen-with-drop .chosen-single div b {
  background-position: -18px 7px;
}
.chosen-container-active .chosen-choices {
  border: 1px solid #66afe9;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
  -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  box-shadow: 0 1px 1px rgba(0, 0, 0, .075) inset, 0 0 8px rgba(82, 168, 236, .6);
  -webkit-transition: border linear .2s, box-shadow linear .2s;
  -o-transition: border linear .2s, box-shadow linear .2s;
  transition: border linear .2s, box-shadow linear .2s;
}
.chosen-container-active .chosen-choices .search-field input[type=\"text\"] {
  color: #111 !important;
}
.chosen-container-active.chosen-with-drop .chosen-choices {
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}
.chosen-disabled {
  cursor: default;
  opacity: 0.5 !important;
}
.chosen-disabled .chosen-single {
  cursor: default;
}
.chosen-disabled .chosen-choices .search-choice .search-choice-close {
  cursor: default;
}
.chosen-rtl {
  text-align: right;
}
.chosen-rtl .chosen-single {
  padding: 0 8px 0 0;
  overflow: visible;
}
.chosen-rtl .chosen-single span {
  margin-left: 26px;
  margin-right: 0;
  direction: rtl;
}
.chosen-rtl .chosen-single div {
  left: 7px;
  right: auto;
}
.chosen-rtl .chosen-single abbr {
  left: 26px;
  right: auto;
}
.chosen-rtl .chosen-choices .search-field input[type=\"text\"] {
  direction: rtl;
}
.chosen-rtl .chosen-choices li {
  float: right;
}
.chosen-rtl .chosen-choices .search-choice {
  margin: 6px 5px 3px 0;
  padding: 3px 5px 3px 19px;
}
.chosen-rtl .chosen-choices .search-choice .search-choice-close {
  background-position: right top;
  left: 4px;
  right: auto;
}
.chosen-rtl.chosen-container-single .chosen-results {
  margin: 0 0 4px 4px;
  padding: 0 4px 0 0;
}
.chosen-rtl .chosen-results .group-option {
  padding-left: 0;
  padding-right: 15px;
}
.chosen-rtl.chosen-container-active.chosen-with-drop .chosen-single div {
  border-right: none;
}
.chosen-rtl .chosen-search input[type=\"text\"] {
  background: url(\"chosen-sprite.png\") no-repeat -28px -20px, #ffffff;
  direction: rtl;
  padding: 4px 5px 4px 20px;
}
@media only screen and (-webkit-min-device-pixel-ratio: 2), only screen and (min-resolution: 144dpi) {
  .chosen-rtl .chosen-search input[type=\"text\"],
  .chosen-container-single .chosen-single abbr,
  .chosen-container-single .chosen-single div b,
  .chosen-container-single .chosen-search input[type=\"text\"],
  .chosen-container-multi .chosen-choices .search-choice .search-choice-close,
  .chosen-container .chosen-results-scroll-down span,
  .chosen-container .chosen-results-scroll-up span {
    background-image: url(\"chosen-sprite@2x.png\") !important;
    background-size: 52px 37px !important;
    background-repeat: no-repeat !important;
  }
}

/*----------------------------------------------------------------------------------------
  ...END OF BOOTSTRAP-CHOSEN SECTION
  bootstrap-chosen additions
----------------------------------------------------------------------------------------*/

/* Mouse hover color in drop-downs */
.chosen-container .chosen-results li.mouseover {
  background-color: #eeeeee;
  background-image: none;
}

  .rc-modal-panel > div:last-child {
    width: 70%;
  }

  ")


