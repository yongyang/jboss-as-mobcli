Ext.ns('Mobcli');

Ext.require('Ext.tab.Panel');

Ext.application({
    launch: function() {

        var inputNodeList = Ext.create('Ext.List',{
            title: "Path://",
            store:  Ext.create("Mobcli.TestStore"),
            itemTpl: '<div>{lastName} - {firstName}</div>',
            onItemDisclosure: true,
            listeners: {
                scope: this,
                itemtap: function() {
                    Ext.Msg.alert("Tap", "Tap");
                },
                disclose: function(el, record, target, index, e, eOpts ) {
                    e.stopEvent();
                    inputNavigationView.push({
                        title: 'Commands',
                        html: 'Command List'
                    });
                }
            }

        });

        var inputNavigationView = Ext.create("Ext.NavigationView",{
            title: 'input',
            items:[inputNodeList]
        });


        Ext.Viewport.add(
            Ext.create("Ext.tab.Panel", {
                id: 'mainTabPanel',
//                icon: 'touch/resources/images/icon1.png',
                activeTab: 0,
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
//                            Ext.create('Mobcli.InputView'),
                            
                    {
                        title: 'Output',
                        id: 'tab3',
                        html: 'Badge labels will truncate if the text is wider than the tab.',
                        badgeText: 'Overflow test',
                        cls: 'card',
                        iconCls: 'download',
                        hidden: (Ext.filterPlatform('ie10') && Ext.os.is.Phone) ? true : false
                    },
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
                    {
                        title: 'About',
                        html: '<p>Docking tabs to the bottom will automatically change their style.</p>',
                        iconCls: 'info',
                        cls: 'card'
                    }
                ]
                }));
    }
});
