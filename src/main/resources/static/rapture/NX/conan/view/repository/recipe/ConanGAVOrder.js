/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * Configuration for Yum hosted repodata level.
 *
 * @since 0.0.2
 */
Ext.define('NX.coreui.view.repository.facet.ConanGAVOrder', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-conan-proxy-gav-facet',
  requires: [
    'NX.I18n',
    'Ext.form.ComboBox'
  ],

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        cls: 'nx-form-section',
        title: NX.I18n.get('Repository_Facet_ConanProxyGAVFacet_Title'),
        items: [
          {
            xtype: 'combo',
            name: 'attributes.conan.conanMatcher',
            fieldLabel: NX.I18n.get('Repository_Facet_ConanProxyGAVFacet_Order_FieldLabel'),
            helpText: NX.I18n.get('Repository_Facet_ConanProxyGAVFacet_Order_HelpText'),
            forceSelection: true,
            editable: false,
            allowBlank: false,
            store : [
                ['remote', NX.I18n.get('Repository_Facet_ConanProxyGAVFacet_Order_GAVItem')],
                ['local', NX.I18n.get('Repository_Facet_ConanProxyGAVFacet_Order_AVGItem')]
            ]
          }
        ]
      }
    ];

    me.callParent();
  }
});

