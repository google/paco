function QXb(){RXb.call(this,false)}
function mYb(a,b){oYb.call(this,a,false);this.c=b}
function nYb(a,b){oYb.call(this,a,false);lYb(this,b)}
function pYb(a){oYb.call(this,'GWT',true);lYb(this,a)}
function Ylb(a){this.d=a;this.c=c6(this.d.b)}
function tXb(a,b){return BXb(a,b,a.b.c)}
function _b(a,b){kc((ue(),pe),a,_H(X0,Fnc,134,[(mcc(),b?lcc:kcc)]))}
function BXb(a,b,c){if(c<0||c>a.b.c){throw new dcc}a.p&&(b.db[lwc]=2,undefined);sXb(a,c,b.db);Eic(a.b,c,b);return b}
function lYb(a,b){a.e=b;!!a.d&&PXb(a.d,a);if(b){(RUb(),b.db).tabIndex=-1;Kf();_b(a.db,true)}else{Kf();_b(a.db,false)}}
function $5(a){var b,c;b=jI(a.b.td(Jwc),148);if(b==null){c=_H(a1,Gnc,1,['New','Open',Kwc,Lwc,'Exit']);a.b.vd(Jwc,c);return c}else{return b}}
function Z5(a){var b,c;b=jI(a.b.td(Iwc),148);if(b==null){c=_H(a1,Gnc,1,['Undo','Redo','Cut','Copy','Paste']);a.b.vd(Iwc,c);return c}else{return b}}
function b6(a){var b,c;b=jI(a.b.td(Owc),148);if(b==null){c=_H(a1,Gnc,1,['Contents','Fortune Cookie','About GWT']);a.b.vd(Owc,c);return c}else{return b}}
function a6(a){var b,c;b=jI(a.b.td(Nwc),148);if(b==null){c=_H(a1,Gnc,1,['Download','Examples',Usc,"GWT wit' the program"]);a.b.vd(Nwc,c);return c}else{return b}}
function _5(a){var b,c;b=jI(a.b.td(Mwc),148);if(b==null){c=_H(a1,Gnc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.vd(Mwc,c);return c}else{return b}}
function sYb(){var a;Zi(this,$doc.createElement(quc));this.db[Bqc]='gwt-MenuItemSeparator';a=$doc.createElement(Gqc);hKb(this.db,a);a[Bqc]='menuSeparatorInner'}
function c6(a){var b,c;b=jI(a.b.td(Pwc),148);if(b==null){c=_H(a1,Gnc,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.vd(Pwc,c);return c}else{return b}}
function Ulb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new Ylb(a);n=new QXb;n.c=true;n.db.style[Cqc]='500px';n.f=true;q=new RXb(true);p=_5(a.b);for(k=0;k<p.length;++k){rXb(q,new mYb(p[k],o))}d=new RXb(true);d.f=true;rXb(n,new nYb('File',d));e=$5(a.b);for(k=0;k<e.length;++k){if(k==3){tXb(d,new sYb);rXb(d,new nYb(e[3],q));tXb(d,new sYb)}else{rXb(d,new mYb(e[k],o))}}b=new RXb(true);rXb(n,new nYb('Edit',b));c=Z5(a.b);for(k=0;k<c.length;++k){rXb(b,new mYb(c[k],o))}f=new RXb(true);rXb(n,new pYb(f));g=a6(a.b);for(k=0;k<g.length;++k){rXb(f,new mYb(g[k],o))}i=new RXb(true);tXb(n,new sYb);rXb(n,new nYb('Help',i));j=b6(a.b);for(k=0;k<j.length;++k){rXb(i,new mYb(j[k],o))}T4b(n.db,aqc,Qwc);OXb(n,Qwc);return n}
var Qwc='cwMenuBar',Iwc='cwMenuBarEditOptions',Jwc='cwMenuBarFileOptions',Mwc='cwMenuBarFileRecents',Nwc='cwMenuBarGWTOptions',Owc='cwMenuBarHelpOptions',Pwc='cwMenuBarPrompts';c2(663,1,{},Ylb);_.sc=function Zlb(){ZKb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;c2(664,1,toc);_.qc=function bmb(){H4(this.c,Ulb(this.b))};c2(1058,104,Inc,QXb);c2(1065,105,{99:1,104:1,118:1},mYb,nYb,pYb);c2(1066,105,{99:1,105:1,118:1},sYb);var kS=Hcc(jvc,'CwMenuBar$1',663),YX=Hcc(hvc,'MenuItemSeparator',1066);gpc(Jn)(7);