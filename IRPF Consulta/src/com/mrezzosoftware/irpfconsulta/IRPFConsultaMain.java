package com.mrezzosoftware.irpfconsulta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mrezzosoftware.irpfconsulta.connection.RFConnection;
import com.mrezzosoftware.irpfconsulta.pf.Declaracao;
import com.mrezzosoftware.irpfconsulta.pf.PessoaFisica;

public class IRPFConsultaMain extends Activity {

	private static final String LOGCAT_TAG = "irpf";
	private EditText txtCpf;
	private AlertDialog.Builder dialogAnos;
	private String[] anosDisponiveis;
	private Button btnAnos;
	private EditText txtCaptcha;
	private Bitmap captcha;
	private ImageView imgRecarregar;
	// Futuro rob� para ler o captcha.
	// private ImageView imgCaptcha;
	private ImageButton btConsultar;
	private RFConnection rfConnection;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.irpfconsultamain);

		rfConnection = new RFConnection();

		txtCpf = (EditText) findViewById(R.id.txtCpf);

		btnAnos = (Button) findViewById(R.id.btnAno);

		carregarAnos();

		btnAnos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialogAnos.create().show();
			}
		});

		txtCaptcha = (EditText) findViewById(R.id.txtCaptcha);
		carregarCaptcha();

		// Futuro rob� para ler o captcha.
		// imgCaptcha = (ImageView) findViewById(R.id.imgCaptcha);

		imgRecarregar = (ImageView) findViewById(R.id.imgRecarregar);
		imgRecarregar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				carregarCaptcha();
			}
		});

		btConsultar = (ImageButton) findViewById(R.id.btnConsultar);
		btConsultar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				consultarDados();
			}
		});

	}

	// Carrega a combo com os anos dispon�veis no site da receita federal.
	private void carregarAnos() {
		new Handler().post(new Runnable() {
			public void run() {
				anosDisponiveis = rfConnection.getAnosDisponiveisConsulta();

				btnAnos.setText((anosDisponiveis.length > 0) ? anosDisponiveis[0]
						: "Anos");

				dialogAnos = new AlertDialog.Builder(IRPFConsultaMain.this)
						.setTitle("Selecione o ano").setItems(anosDisponiveis,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											final int which) {
										// final int id = which;
										new Handler().post(new Runnable() {
											public void run() {
												btnAnos.setText(anosDisponiveis[which]);
												btnAnos.invalidate();
											}
										});
									}
								});
			}
		});
	}

	// Carrega o captcha do site da receita federal.
	private void carregarCaptcha() {
		new Handler().post(new Runnable() {
			public void run() {

				// RFConnection rfConnection = new RFConnection();
				captcha = rfConnection
						.getImage(RFConnection.URL_CAPTCHA_RECEITA);

				if (captcha != null) {
					ImageView imgView = (ImageView) findViewById(R.id.imgCaptcha);
					imgView.setImageBitmap(captcha);
				} else {
					Toast.makeText(
							IRPFConsultaMain.this,
							"Falha ao obter captcha do site da Receita Federal.\nTente novamente em alguns segundos.",
							Toast.LENGTH_SHORT);
				}

			}
		});
	}

	protected void consultarDados() {
		new Handler().post(new Runnable() {

			public void run() {
				PessoaFisica pessoa = new PessoaFisica(txtCpf.getText()
						.toString(), btnAnos.getText().toString(), txtCaptcha
						.getText().toString());

				logInfo("----------PESSOA----------");
				logInfo("CPF: " + pessoa.getCpf());
				logInfo("Ano: " + pessoa.getAno());
				logInfo("Captcha: " + pessoa.getCaptcha());
				logInfo("----------PESSOA----------");

				Declaracao declaracao = rfConnection.consultarDadosRF(
						IRPFConsultaMain.this, pessoa);
				reciclarObjeto(pessoa);

				logInfo("C�digo erro da declara��o?: "
						+ declaracao.getCodigoRetorno());

				if (declaracao != null) {
					if (declaracao.getCodigoRetorno() == Declaracao.OK) {

						logInfo("---------------------------------Vai iniciar a activity---------------------------------");

						Intent intentResultado = new Intent(IRPFConsultaMain.this,
								IRPFResultadoConsulta.class);
						intentResultado.putExtra("declaracao", declaracao);
						startActivity(intentResultado);
						txtCpf.setText("");
						
					} else if (declaracao.getCodigoRetorno() == Declaracao.CPF_INVALIDO) {
						Toast.makeText(IRPFConsultaMain.this, "CPF inv�lido!",
								Toast.LENGTH_SHORT).show();
						txtCpf.sett
					} else if (declaracao.getCodigoRetorno() == Declaracao.CAPTCHA_INVALIDO) {
						Toast.makeText(IRPFConsultaMain.this,
								"Captcha incorreto!", Toast.LENGTH_SHORT)
								.show();
					} else if (declaracao.getCodigoRetorno() == Declaracao.ERRO_HABILITACAO) {
						Toast.makeText(
								IRPFConsultaMain.this,
								"Site da Receita Federal indispon�vel\nTente mais tarde",
								Toast.LENGTH_SHORT).show();
					}

				}
				
				txtCaptcha.setText("");
				carregarCaptcha();
			}
		});
	}

	public static void reciclarObjeto(Object o) {
		o = null;
		System.gc();
	}

	public static void logError(Throwable error) {
		Log.e(IRPFConsultaMain.LOGCAT_TAG, error.getMessage(), error);
	}

	public static void logInfo(String message) {
		Log.i(IRPFConsultaMain.LOGCAT_TAG, message);
	}
}