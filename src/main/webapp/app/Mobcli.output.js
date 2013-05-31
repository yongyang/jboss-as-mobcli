Ext.define('Mobcli.output.Panel', {
    extend: 'Ext.Panel',
    config: {
        layout: 'vbox',
        scrollable: 'both',
        defaults: {
            padding: 5
        },
        items: [
            {
                xtype: 'toolbar',
                docked: 'top',
                title: 'Output'
            }
        ]
    },
    initialize: function() {
        this.callParent();
        // scroll to end automatically
        this.getScrollable().getScroller().on('maxpositionchange', function(scroller, max) {
            scroller.scrollTo(0, max.y);
        });
    },
    print: function(addressPath, operationResult) {
        this.add(Ext.create('Ext.Label',
                {
                    html: addressPath + "<br><pre>" + operationResult + "</pre> <hr>"
                }
        ));
    }
});