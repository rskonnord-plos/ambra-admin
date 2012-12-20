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
var changeHidden = function () {
  var orgEl = document.getElementById("orgVisibility");
  if (orgEl.value === "private") {
    orgEl.value = "public";
  } else {
    orgEl.value = "private";
  }
};

/**
 * ambra.formUtil.selectAllCheckboxes(Form field  srcObj, Form field  targetCheckboxObj)
 *
 * If the form field srcObj has been selected, all the checkboxes in the form field targetCheckboxObj
 * gets selected.  When srcObj is not selected, all the checkboxes in targetCheckboxObj gets
 * deselected.
 *
 * @param			srcObj							Form field				Checkbox field.
 * @param			targetCheckboxObj		Form field				Checkbox field.
 */
var selectAllCheckboxes = function (srcObj, targetCheckboxObj) {
  if (srcObj.checked) {
    //if user has only one row of data, targetCheckboxObj is not array
    // and hence there is no length property.
    if(targetCheckboxObj.length) {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = true;
      }
    }
    else {
      targetCheckboxObj.checked = true;
    }
  }
  else {
    if(targetCheckboxObj.length) {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = false;
      }
    }
    else {
      targetCheckboxObj.checked = false;
    }
  }
};

/**
 * ambra.formUtil.selectCheckboxPerCollection(Form field  srcObj, Form field  collectionObj)
 *
 * Checks to see if all of the checkboxes in the collectionObj are selected.  If it is, select srcObj
 * also.  If all of the checkboxes in collectionObj are not selected, deselect srcObj.
 *
 * @param			srcObj							Form field				Checkbox field.
 * @param			targetCheckboxObj		Form field				Checkbox field.
 */
var selectCheckboxPerCollection = function (srcObj, collectionObj) {
  var count = 0;

  for (var i=0; i<collectionObj.length; i++) {
    if (collectionObj[i].checked)
      count++;
  }

  srcObj.checked = (count == collectionObj.length) ? true : false;
};


