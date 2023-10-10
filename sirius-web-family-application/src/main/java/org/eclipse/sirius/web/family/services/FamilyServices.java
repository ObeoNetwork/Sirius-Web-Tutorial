/*******************************************************************************
 * Copyright (c) 2023 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.family.services;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class FamilyServices {

    // aql:self.name + ' ' + self.eContainer().name
    public String fullName(EObject person) {
        EObject parent = person.eContainer();
        if (this.isInstance(parent, "tutorial", "Family")) {
            String familyName = (String) this.getAttribute(parent, "name");
            String ownName = (String) this.getAttribute(person, "name");
            return ownName + " " + familyName;
        }
        return "";
    }

    private Object getAttribute(EObject object, String attributeName) {
        return object.eGet(object.eClass().getEStructuralFeature(attributeName));
    }

    private boolean isInstance(EObject object, String domain, String typeName) {
        if (object != null) {
            EClass klass = object.eClass();
            return klass.getName().equals(typeName) && klass.getEPackage().getName().equals(domain);
        } else {
            return false;
        }
    }

}
