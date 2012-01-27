/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 1/26/12
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
/**
 * js in support of admin UI.
 *
 * @author jsuttor
 */
function setCheckBoxes(formName, fieldQuery, checkValue) {

  // form must exist
  if (!document.forms[formName]) { return; }

  // get all named fields
  var objCheckBoxes = dojo.query(fieldQuery,document.forms[formName]);

  if (!objCheckBoxes) { return; }

  // set the check value for all check boxes
  var countCheckBoxes = objCheckBoxes.length;
  if (!countCheckBoxes) {
    objCheckBoxes.checked = checkValue;
    colorRow(objCheckBoxes.id, checkValue);
    checkRow(objCheckBoxes.id, checkValue);
    checkSyndications(objCheckBoxes.id, checkValue);
  } else {
    for (var i = 0; i < countCheckBoxes; i++) {
      objCheckBoxes[i].checked = checkValue;
      colorRow(objCheckBoxes[i].id, checkValue);
      checkRow(objCheckBoxes[i].id, checkValue);
      checkSyndications(objCheckBoxes[i].id, checkValue);
    }
  }
}

function colorRow(checkBoxID, elementValue)
{
  var rowID = "tr_" + checkBoxID;
  var row = dojo.byId(rowID);

  if(row != null) {
    if(elementValue) {
      row.setAttribute("style", "background:#99CCFF;");
    } else {
      row.removeAttribute("style");
    }
  }
}

function checkRow(checkBoxID, elementValue)
{
  var IDs = checkBoxID.split("_");

  if(IDs.length) {
    var articleCheckBox = dojo.byId(IDs[0]);

    if(elementValue && articleCheckBox != null) {
      articleCheckBox.checked = true;
      colorRow(IDs[0], true);
    }
  }
}

function checkSyndications(checkBoxID, elementValue)
{
  //Let's only have dojo search the current row
  var rowID = "tr_" + checkBoxID;
  var rowObj = dojo.byId(rowID);

  var syndicationCheckBoxes = dojo.query('[id^=' + checkBoxID + '_]',rowObj);

  for(var a = 0; a < syndicationCheckBoxes.length; a++){
    syndicationCheckBoxes[a].checked = elementValue;
  }
}

function checkValues(articleCheckBox)
{
  checkSyndications(articleCheckBox.id, articleCheckBox.checked);
  colorRow(articleCheckBox.id,articleCheckBox.checked);
}

function confirmDisableArticles()
{
  return confirm('Are you sure you want to disable the selected articles?');
}

function confirmDisableArticle()
{
  return confirm('Are you sure you want to disable this article?');
}

function confirmUnpublishArticle() {
  return confirm('Are you sure you want to unpublish this article?');
}

function pubDateSort(element) {
  var inputElem = document.createElement("input");
  inputElem.setAttribute("type", "hidden");
  inputElem.setAttribute("name", "action");
  inputElem.setAttribute("value", "Sort by Pub Date " + element.innerHTML);

  element.form.appendChild(inputElem);
  element.form.submit();
}

function articleSort(element) {
  var inputElem = document.createElement("input");
  inputElem.setAttribute("type", "hidden");
  inputElem.setAttribute("name", "action");
  inputElem.setAttribute("value", "Sort by DOI " + element.innerHTML);

  element.form.appendChild(inputElem);
  element.form.submit();
}


