/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*global Ext, NX*/

/**
 * Conan plugin strings.
 */
Ext.define('NX.conan.app.PluginStrings', {
  '@aggregate_priority': 90,

  singleton: true,
  requires: [
    'NX.I18n'
  ],

  keys: {
    Repository_Facet_ConanFacet_Title: 'Conan Settings',
    SearchConan_Group: 'Conan Repositories',
    SearchConan_License_FieldLabel: 'License',
    SearchConan_Text: 'Conan',
    SearchConan_Description: 'Search for components in Conan repositories',

    Repository_Facet_ConanProxyGAVFacet_Title: 'GAV Ordering',
    Repository_Facet_ConanProxyGAVFacet_Order_FieldLabel: 'GAV ordering',
    Repository_Facet_ConanProxyGAVFacet_Order_HelpText: 'Servers can response using various path sequences',
    Repository_Facet_ConanProxyGAVFacet_Order_GAVItem: 'remote',
    Repository_Facet_ConanProxyGAVFacet_Order_AVGItem: 'local'
  }
}, function(self) {
  NX.I18n.register(self);
});
