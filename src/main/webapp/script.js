// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/** Add custom field to form while creating an event. */
function showCustomFieldName() {
  document.getElementById('add-event-custom-fields').hidden = true;
  document.getElementById('add-event-custom-field').hidden = false;
}

function addCustomField() {
  document.getElementById('add-event-custom-fields').hidden = false;
  document.getElementById('add-event-custom-field').hidden = true;

  var form = document.getElementById('add-event-form');
  
  var fieldName = document.getElementById('field-name').value;
  document.getElementById('field-name').value = '';
  document.getElementById('event-custom-fields').value += ',' + fieldName;
  

  const fieldLabel = document.createElement('label');
  fieldLabel.setAttribute('for', fieldName);
  fieldLabel.innerText = fieldName;

  const fieldInput = document.createElement('input');
  fieldInput.setAttribute('type', 'text');
  fieldInput.setAttribute('id', fieldName);
  fieldInput.setAttribute('name', fieldName);

  var button = document.getElementById('add-event-custom-fields');
  form.insertBefore(fieldLabel, button);
  form.insertBefore(fieldInput, button);
}

function addPerson() {
  var person = document.getElementById('person').value;
  document.getElementById('person').value = '';
  document.getElementById('event-people').value += ',' + person;
}
