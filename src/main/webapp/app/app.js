Ext.require('Ext.tab.Panel');

Ext.define('model1', {
    extend: 'Ext.data.Model',
    config: {
        fields: ['firstName', 'lastName']
    }
});

Ext.define("list1", {
    extend: 'Ext.data.Store',
    alias: 'store.List',
    config: {
        model: 'model1',
        sorters: 'firstName',
        grouper: function(record) {
            return record.get('firstName')[0];
        },
        data: [
            {firstName: 'Julio', lastName: 'Benesh'},
            {firstName: 'Julio', lastName: 'Minich'},
            {firstName: 'Tania', lastName: 'Ricco'},
            {firstName: 'Odessa', lastName: 'Steuck'},
            {firstName: 'Nelson', lastName: 'Raber'},
            {firstName: 'Tyrone', lastName: 'Scannell'},
            {firstName: 'Allan', lastName: 'Disbrow'},
            {firstName: 'Cody', lastName: 'Herrell'},
            {firstName: 'Julio', lastName: 'Burgoyne'},
            {firstName: 'Jessie', lastName: 'Boedeker'},
            {firstName: 'Allan', lastName: 'Leyendecker'},
            {firstName: 'Javier', lastName: 'Lockley'},
            {firstName: 'Guy', lastName: 'Reasor'},
            {firstName: 'Jamie', lastName: 'Brummer'},
            {firstName: 'Jessie', lastName: 'Casa'},
            {firstName: 'Marcie', lastName: 'Ricca'},
            {firstName: 'Gay', lastName: 'Lamoureaux'},
            {firstName: 'Althea', lastName: 'Sturtz'},
            {firstName: 'Kenya', lastName: 'Morocco'},
            {firstName: 'Rae', lastName: 'Pasquariello'},
            {firstName: 'Ted', lastName: 'Abundis'},
            {firstName: 'Jessie', lastName: 'Schacherer'},
            {firstName: 'Jamie', lastName: 'Gleaves'},
            {firstName: 'Hillary', lastName: 'Spiva'},
            {firstName: 'Elinor', lastName: 'Rockefeller'},
            {firstName: 'Dona', lastName: 'Clauss'},
            {firstName: 'Ashlee', lastName: 'Kennerly'},
            {firstName: 'Alana', lastName: 'Wiersma'},
            {firstName: 'Kelly', lastName: 'Holdman'},
            {firstName: 'Mathew', lastName: 'Lofthouse'},
            {firstName: 'Dona', lastName: 'Tatman'},
            {firstName: 'Clayton', lastName: 'Clear'},
            {firstName: 'Rosalinda', lastName: 'Urman'},
            {firstName: 'Cody', lastName: 'Sayler'},
            {firstName: 'Odessa', lastName: 'Averitt'},
            {firstName: 'Ted', lastName: 'Poage'},
            {firstName: 'Penelope', lastName: 'Gayer'},
            {firstName: 'Katy', lastName: 'Bluford'},
            {firstName: 'Kelly', lastName: 'Mchargue'},
            {firstName: 'Kathrine', lastName: 'Gustavson'},
            {firstName: 'Kelly', lastName: 'Hartson'},
            {firstName: 'Carlene', lastName: 'Summitt'},
            {firstName: 'Kathrine', lastName: 'Vrabel'},
            {firstName: 'Roxie', lastName: 'Mcconn'},
            {firstName: 'Margery', lastName: 'Pullman'},
            {firstName: 'Avis', lastName: 'Bueche'},
            {firstName: 'Esmeralda', lastName: 'Katzer'},
            {firstName: 'Tania', lastName: 'Belmonte'},
            {firstName: 'Malinda', lastName: 'Kwak'},
            {firstName: 'Tanisha', lastName: 'Jobin'},
            {firstName: 'Kelly', lastName: 'Dziedzic'},
            {firstName: 'Darren', lastName: 'Devalle'},
            {firstName: 'Julio', lastName: 'Buchannon'},
            {firstName: 'Darren', lastName: 'Schreier'},
            {firstName: 'Jamie', lastName: 'Pollman'},
            {firstName: 'Karina', lastName: 'Pompey'},
            {firstName: 'Hugh', lastName: 'Snover'},
            {firstName: 'Zebra', lastName: 'Evilias'}
        ]
    }
});

Ext.application({
    launch: function() {
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
                    {
                        title: 'Input',
                        xtype: 'list',
                        store:  Ext.create("list1"),
                        itemTpl: '<div>{lastName} - {firstName}</div>',
                        onItemDisclosure: function(record, btn, index) {
                            Ext.Msg.alert('Tap', 'Disclore more info of ' + index + ', ' + record);
                        },
                        iconCls: 'search',
                        cls: 'card dark',
                        badgeText: '4'
                    },
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
