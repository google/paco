function SWb(){TWb.call(this,false)}
function oXb(a,b){qXb.call(this,a,false);this.c=b}
function pXb(a,b){qXb.call(this,a,false);nXb(this,b)}
function rXb(a){qXb.call(this,'GWT',true);nXb(this,a)}
function blb(a){this.d=a;this.c=h5(this.d.b)}
function wWb(a,b){return DWb(a,b,a.b.c)}
function Pb(a,b){$b((ie(),de),a,nH(a0,qmc,134,[(_ac(),b?$ac:Zac)]))}
function nXb(a,b){a.e=b;!!a.d&&RWb(a.d,a);if(b){b.db.tabIndex=-1;yf();Pb(a.db,true)}else{yf();Pb(a.db,false)}}
function DWb(a,b,c){if(c<0||c>a.b.c){throw new Sac}a.p&&(b.db[Suc]=2,undefined);vWb(a,c,b.db);qhc(a.b,c,b);return b}
function d5(a){var b,c;b=xH(a.b.ld(ovc),148);if(b==null){c=nH(f0,rmc,1,['New','Open',pvc,qvc,'Exit']);a.b.nd(ovc,c);return c}else{return b}}
function c5(a){var b,c;b=xH(a.b.ld(nvc),148);if(b==null){c=nH(f0,rmc,1,['Undo','Redo','Cut','Copy','Paste']);a.b.nd(nvc,c);return c}else{return b}}
function g5(a){var b,c;b=xH(a.b.ld(tvc),148);if(b==null){c=nH(f0,rmc,1,['Contents','Fortune Cookie','About GWT']);a.b.nd(tvc,c);return c}else{return b}}
function f5(a){var b,c;b=xH(a.b.ld(svc),148);if(b==null){c=nH(f0,rmc,1,['Download','Examples',zrc,"GWT wit' the program"]);a.b.nd(svc,c);return c}else{return b}}
function e5(a){var b,c;b=xH(a.b.ld(rvc),148);if(b==null){c=nH(f0,rmc,1,['Fishing in the desert.txt','How to tame a wild parrot','Idiots Guide to Emu Farms']);a.b.nd(rvc,c);return c}else{return b}}
function uXb(){var a;Ni(this,$doc.createElement(Ysc));this.db[lpc]='gwt-MenuItemSeparator';a=$doc.createElement(qpc);mJb(this.db,a);a[lpc]='menuSeparatorInner'}
function h5(a){var b,c;b=xH(a.b.ld(uvc),148);if(b==null){c=nH(f0,rmc,1,['Thank you for selecting a menu item','A fine selection indeed',"Don't you have anything better to do than select menu items?",'Try something else','this is just a menu!','Another wasted click']);a.b.nd(uvc,c);return c}else{return b}}
function Zkb(a){var b,c,d,e,f,g,i,j,k,n,o,p,q;o=new blb(a);n=new SWb;n.c=true;n.db.style[mpc]='500px';n.f=true;q=new TWb(true);p=e5(a.b);for(k=0;k<p.length;++k){uWb(q,new oXb(p[k],o))}d=new TWb(true);d.f=true;uWb(n,new pXb('File',d));e=d5(a.b);for(k=0;k<e.length;++k){if(k==3){wWb(d,new uXb);uWb(d,new pXb(e[3],q));wWb(d,new uXb)}else{uWb(d,new oXb(e[k],o))}}b=new TWb(true);uWb(n,new pXb('Edit',b));c=c5(a.b);for(k=0;k<c.length;++k){uWb(b,new oXb(c[k],o))}f=new TWb(true);uWb(n,new rXb(f));g=f5(a.b);for(k=0;k<g.length;++k){uWb(f,new oXb(g[k],o))}i=new TWb(true);wWb(n,new uXb);uWb(n,new pXb('Help',i));j=g5(a.b);for(k=0;k<j.length;++k){uWb(i,new oXb(j[k],o))}V3b(n.db,Moc,vvc);QWb(n,vvc);return n}
var vvc='cwMenuBar',nvc='cwMenuBarEditOptions',ovc='cwMenuBarFileOptions',rvc='cwMenuBarFileRecents',svc='cwMenuBarGWTOptions',tvc='cwMenuBarHelpOptions',uvc='cwMenuBarPrompts';h1(658,1,{},blb);_.oc=function clb(){cKb(this.c[this.b]);this.b=(this.b+1)%this.c.length};_.b=0;_.d=null;h1(659,1,enc);_.mc=function glb(){M3(this.c,Zkb(this.b))};h1(1053,102,tmc,SWb);h1(1060,103,{99:1,104:1,118:1},oXb,pXb,rXb);h1(1061,103,{99:1,105:1,118:1},uXb);var sR=ubc(Qtc,'CwMenuBar$1',658),eX=ubc(Otc,'MenuItemSeparator',1061);Tnc(wn)(7);