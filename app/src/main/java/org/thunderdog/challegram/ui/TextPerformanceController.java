package org.thunderdog.challegram.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.data.PageBlockRichText;
import org.thunderdog.challegram.data.TD;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.util.text.FormattedText;
import org.thunderdog.challegram.util.text.Text;
import org.thunderdog.challegram.util.text.TextColorSets;
import org.thunderdog.challegram.widget.PageBlockView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TextPerformanceController extends ViewController<TdApi.WebPageInstantView> {
  private FormattedText[] formattedTexts;

  public TextPerformanceController (@NonNull Context context, Tdlib tdlib) {
    super(context, tdlib);
  }

  @Override
  protected View onCreateView (Context context) {
    ArrayList<TdApi.RichText> texts = new ArrayList<>();
    TD.getTexts(texts, getArgumentsStrict().pageBlocks);

    ArrayList<FormattedText> formattedTextsL = new ArrayList<>(texts.size());
    for (int a = 0; a < texts.size(); a++) {
      FormattedText f = FormattedText.parseRichText(this, texts.get(a), null);
      if (f != null) {
        formattedTextsL.add(f);
      }
    }

    this.formattedTexts = formattedTextsL.toArray(new FormattedText[0]);


    FrameLayout frameLayout = new FrameLayout(context) {
      @Override
      protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        start();
      }
    };

    frameLayout.setBackgroundColor(0xFF00FF00);

    return frameLayout;
  }


  private static final int REPEATS = 100;

  private int times = 0;
  private long total = 0;

  private void start () {
    if (times > 0 || isDestroyed()) {
      return;
    }

    times = REPEATS;
    total = 0;

    Log.i("WTF_DEBUG", "Build Start: " + formattedTexts.length);
    run();
  }

  private void run () {
    if (times == 0 || isDestroyed()) {
      Log.i("WTF_DEBUG", "Build End: " + TimeUnit.NANOSECONDS.toMillis(total / REPEATS));
      return;
    }

    final int width = getValue().getMeasuredWidth();
    final Text[] texts = new Text[formattedTexts.length];
    long s = System.nanoTime();

    for (int N = 0; N < 1; N++) {
      for (int a = 0; a < formattedTexts.length; a++) {
        FormattedText formattedText = formattedTexts[a];
        Text.Builder b = new Text.Builder(formattedText.text, width, PageBlockRichText.getParagraphProvider(), TextColorSets.InstantView.NORMAL).entities(formattedText.entities, null);
        texts[a] = b.build();
      }
    }

    long e = System.nanoTime();
    total += (e - s);




    times -= 1;

    UI.post(this::run);
  }


  @Override
  public int getId () {
    return 0;
  }
}
