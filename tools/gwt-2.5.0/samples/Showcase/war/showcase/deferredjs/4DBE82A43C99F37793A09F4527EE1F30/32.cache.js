function ND(){ND=pnc;MD=new Ukc}
function zUb(a,b,c,d){var e;a.a.Wf(b,c);e=BUb(a.a.i,b,c);cj(e,d,true)}
function OD(d,a){var b=d.a;for(var c in b){b.hasOwnProperty(c)&&a.ld(c)}}
function QD(d,a){a=String(a);var b=d.a;var c=b[a];(c==null||!b.hasOwnProperty(a))&&d.bd(a);return String(c)}
function SD(){ND();var a;a=UH(MD.fd(bzc),61);if(!a){a=new RD;MD.hd(bzc,a)}return a}
function PD(c,b){try{typeof $wnd[b]!='object'&&UD(b);c.a=$wnd[b]}catch(a){UD(b)}}
function UD(a){throw new Vlc($rc+a+"' is not a JavaScript object and cannot be used as a Dictionary")}
function RD(){this.b='Dictionary userInfo';PD(this,bzc);if(!this.a){throw new Vlc("Cannot find JavaScript object with the name 'userInfo'")}}
function lkb(){var a,b,c,d,e,f,g,i,j,k,n;f=new n5b;g=new QRb('<pre>var userInfo = {\n&nbsp;&nbsp;name: "Amelie Crutcher",\n&nbsp;&nbsp;timeZone: "EST",\n&nbsp;&nbsp;userID: "123",\n&nbsp;&nbsp;lastLogOn: "2/2/2006"\n};<\/pre>\n');g.cb.dir=urc;g.cb.style['textAlign']=Orc;k5b(f,new QRb('<b>\u8FD9\u4E2A\u4F8B\u5B50\u4F7F\u7528\u4E0B\u5217Javascript\u7684\u53D8\u91CF\uFF1A <\/b>'));k5b(f,g);j=new rUb;b=j.j;i=SD();e=(n=new alc,OD(i,n),n);a=0;for(d=Yhc(TE(e.a));d.a.rd();){c=UH(cic(d),1);k=QD(i,c);iUb(j,0,a,c);zUb(b,0,a,'cw-DictionaryExample-header');iUb(j,1,a,k);zUb(b,1,a,'cw-DictionaryExample-data');++a}k5b(f,new QRb('<br><br>'));k5b(f,j);return f}
var bzc='userInfo';K1(345,1,{61:1},RD);_.bd=function TD(a){var b;b="Cannot find '"+a+"' in "+this;throw new Vlc(b)};_.tS=function VD(){return this.b};_.a=null;_.b=null;var MD;K1(644,1,joc);_.lc=function rkb(){s4(this.a,lkb())};var hN=zcc(evc,'Dictionary',345);Yoc(vn)(32);