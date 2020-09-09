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

var key  = config.PLACES_API_KEY;
var script = document.createElement('script');
script.src = 'https://maps.googleapis.com/maps/api/js?key=' + key + '&callback=initLocation&libraries=places';
script.defer = true;

document.head.appendChild(script);