Ext.ns('Mobcli');

Ext.require('Ext.tab.Panel');

Ext.application({
    launch: function() {

        var inputNavigationView = Ext.create("Mobcli.input.NavigationView", 
            {
                id: 'inputNavigationView',
                title: 'Input',
                cls: 'card dark',
                iconCls: 'search',
                items: [
                    Ext.create('Mobcli.input.NodeListView',                {
                        title: 'PATH:',
                        address: '~',
                        store: {
                            data: [
                                Ext.create('Mobcli.input.NodeModel', {
                                    name: '/', 
                                    address: '/', 
                                    value: '/', 
                                    displayname: '/', 
                                    leaf: false,
                                    generic: false}).getData()
                            ]
                        }
                    })            
                ]
            });
        
        var outputPanel = Ext.create('Mobcli.output.Panel', 
            {
                id: 'ID_OutputPanel',
                title: 'Output',
                cls: 'card dark',
                iconCls: 'search'
            });

        var tabPanel =  Ext.create("Ext.tab.Panel", {
                id: 'mainTabPanel',
                ui: 'dark',
                tabBar: {
//                    ui: Ext.filterPlatform('blackberry') || Ext.filterPlatform('ie10') ? 'dark' : 'light',
                    layout: {
                        pack : 'center',
                        align: 'center'
                    },
                    docked: 'bottom'
                },
                defaults: {
                    scrollable: true
                },
                items: [
                    inputNavigationView,
                    outputPanel,
                    {
                        title: 'Monitor',
                        html: 'Tabbars are <code>ui:"dark"</code> by default, but also have light variants.',
                        cls: 'card dark',
                        iconCls: 'time',
                        hidden: (Ext.filterPlatform('ie10') && Ext.os.is.Phone) ? true : false
                    },
                    {
                        title: 'Settings',
                        html: 'Tabbars are <code>ui:"dark"</code> by default, but also have light variants.',
                        cls: 'card dark',
                        iconCls: 'settings',
                        hidden: (Ext.filterPlatform('ie10') && Ext.os.is.Phone) ? true : false
                    },
                    Ext.create('Mobcli.about.AboutPanel')
                ]
                });

        Ext.Viewport.add(tabPanel);
    }
});
