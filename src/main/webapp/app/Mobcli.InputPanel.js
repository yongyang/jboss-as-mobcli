Ext.define('Mobcli.TestModel', {
    extend: 'Ext.data.Model',
    config: {
        fields: ['firstName', 'lastName']
    }
});

Ext.define("Mobcli.TestStore", {
    extend: 'Ext.data.Store',
    alias: 'store.List',
    config: {
        model: 'Mobcli.TestModel',
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
            {firstName: 'Zebra', lastName: 'Evilias'}
        ]
    }
});

/*
var l = Ext.create("Ext.List", {
    store:  Ext.create("Mobcli.TestStore"),
    itemTpl: '<div>{lastName} - {firstName}</div>',
    onItemDisclosure: function(record, btn, index) {
        Ext.Msg.alert('Tap', 'Disclose more info of ' + index + ', ' + record);
    }
});

*/

Ext.define('Mobcli.NodeList',
    {
        extend: 'Ext.List',
        config: {
            title: "Path://",
                    store:  Ext.create("Mobcli.TestStore"),
                    itemTpl: '<div>{lastName} - {firstName}</div>',
                    onItemDisclosure: true,
                    listeners: {
                        scope: this,
                        itemtap: function() {
                            Ext.Msg.alert("Tap", "Tap");
                            return false;
                        },
                        disclose: function(el, record, target, index, e, eOpts ) {
                            e.stopEvent();
                            Ext.Msg.alert("Dis", "Disclose");
                            return false;
                        }
                    }

        },
        
        constructor: function(config) {
            config = config?  config : {};
            this.callParent(config);
            this.initConfig(config)
        }
    }
);

Ext.define('Mobcli.InputView',{
    extend: 'Ext.NavigationView',
    config: {
        title: 'input'
/*
        defaults: {
            layout: 'fit'
        },
*/
    },
    constructor: function(config) {
        config = config?  config : {};
        this.callParent(config);
        this.initConfig(config);
    }
});

