/*
    This file is part of Bromite.

    Bromite is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bromite is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bromite. If not, see <https://www.gnu.org/licenses/>.
*/

#ifndef SITE_ENGAGEMENT_CORE_FEATURES_H_
#define SITE_ENGAGEMENT_CORE_FEATURES_H_

#include <string>

#include "base/feature_list.h"

namespace site_engagement {
namespace features {

// Enable site engagement
extern const base::Feature kSiteEngagement;

}  // namespace features
}  // namespace site_engagement

#endif  // SITE_ENGAGEMENT_CORE_FEATURES_H_
