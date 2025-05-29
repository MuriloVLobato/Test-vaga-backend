package com.testebackendjr.controller;
import com.testebackendjr.model.PontosInteresse;
import com.testebackendjr.model.PontosInteresseDTO;
import com.testebackendjr.repository.PontosInteresseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PontoInteresseController {

    private final PontosInteresseRepository repository;

    public PontoInteresseController(PontosInteresseRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/pontos-de-interesse")
    public ResponseEntity<Void> pontosInteresseCriar(@RequestBody PontosInteresseDTO body) {
        repository.save(new PontosInteresse(body.nome(), body.x(), body.y()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/listar/pontos-de-interesse")
    public ResponseEntity<List<PontosInteresse>> pontosInteresseListar() {
        List<PontosInteresse> listaInteresses = repository.findAll();
        return ResponseEntity.ok(listaInteresses);
    }

    @GetMapping("/listar/pontos-proximos")
    public ResponseEntity<List<PontosInteresse>> listarPontosProximos(
            @RequestParam("x") Long x,
            @RequestParam("y") Long y,
            @RequestParam("raio") Long raio) {

        long xMin = x - raio;
        long xMax = x + raio;
        long yMin = y - raio;
        long yMax = y + raio;

        List<PontosInteresse> pontosFiltrados = repository.findPontosInteresseProximos(xMin, xMax, yMin, yMax)
                .stream()
                .filter(p -> distanciaEuclidiana(x, y, p.getX(), p.getY()) <= raio)
                .toList();
        return ResponseEntity.ok(pontosFiltrados);
    }

    public double distanciaEuclidiana(long x1, long y1, long x2, long y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }
}
